package com.teachmeski.app.ui.wallet

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.TokenWallet
import com.teachmeski.app.domain.repository.WalletRepository
import com.teachmeski.app.iap.IapError
import com.teachmeski.app.iap.IapManager
import com.teachmeski.app.iap.IapManagerImpl
import com.teachmeski.app.iap.IapProduct
import com.teachmeski.app.iap.VerifyPurchaseClient
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val wallet: TokenWallet? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val products: List<IapProduct> = emptyList(),
    val productsLoading: Boolean = false,
    val productsError: UiText? = null,
    val iapPhase: IapPhase = IapPhase.Idle,
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val iapManager: IapManager,
    private val verifyPurchaseClient: VerifyPurchaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    /** One-shot snackbar signals. The UI layer renders and clears. */
    private val _snackbarMessages = MutableSharedFlow<UiText>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<UiText> = _snackbarMessages.asSharedFlow()

    init {
        load()
        loadProducts()
        recoverPendingPurchases()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = walletRepository.getWallet()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(wallet = result.data, isLoading = false, error = null)
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }

                Resource.Loading -> {
                    _uiState.update { s -> s.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(productsLoading = true, productsError = null) }
            iapManager.loadProducts()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(products = list, productsLoading = false, productsError = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            productsLoading = false,
                            productsError = UiText.StringResource(R.string.wallet_iap_products_error),
                        )
                    }
                }
        }
    }

    /** Called on screen enter AND lifecycle ON_START. Safe to invoke repeatedly. */
    fun recoverPendingPurchases() {
        viewModelScope.launch {
            val pending = runCatching { iapManager.queryPendingPurchases() }.getOrNull().orEmpty()
            // A purchase appears here if we credited but failed to consume, OR if Play
            // returned OK but our process was killed before verify. The RPC is idempotent,
            // so re-running verify+consume is always safe.
            for (purchase in pending) {
                if (purchase.purchaseState != com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) continue
                val productId = purchase.products.firstOrNull() ?: continue
                runVerifyAndConsume(productId, purchase.purchaseToken, showSnackbarOnFail = false)
            }
        }
    }

    fun onPackageTapped(activity: Activity, productId: String) {
        if (_uiState.value.iapPhase !is IapPhase.Idle) return
        viewModelScope.launch {
            _uiState.update { it.copy(iapPhase = IapPhase.Purchasing(productId)) }
            val result = iapManager.launchPurchase(activity, productId)
            val purchase = result.getOrElse { throwable ->
                handlePurchaseFailure(throwable)
                return@launch
            }
            runVerifyAndConsume(productId, purchase.purchaseToken, showSnackbarOnFail = true)
        }
    }

    private suspend fun runVerifyAndConsume(
        productId: String,
        purchaseToken: String,
        showSnackbarOnFail: Boolean,
    ) {
        _uiState.update { it.copy(iapPhase = IapPhase.Verifying(productId)) }

        val verifyResult = verifyPurchaseClient.verify(productId, purchaseToken)
        val response = verifyResult.getOrElse { throwable ->
            // Keep the token around (Play retains it until consumed). Next onStart
            // or next Wallet open will hit recoverPendingPurchases() and retry.
            _uiState.update { it.copy(iapPhase = IapPhase.WebhookDelayed) }
            if (showSnackbarOnFail) {
                val code = (throwable as? VerifyPurchaseClient.VerifyPurchaseException)?.iapError?.code
                val msg = when (code) {
                    "purchase_pending" -> R.string.wallet_iap_pending
                    else -> R.string.wallet_iap_webhook_delayed
                }
                _snackbarMessages.tryEmit(UiText.StringResource(msg))
            }
            return
        }

        // verify returned. Only consume if actually credited (first time through);
        // if credited==false (already-processed replay), still consume to finalize.
        runCatching { iapManager.consume(purchaseToken) }
            .onFailure { /* non-fatal — acknowledged server-side already */ }

        val wallet = _uiState.value.wallet
        _uiState.update {
            it.copy(
                wallet = wallet?.copy(balance = response.newBalance) ?: wallet,
                iapPhase = IapPhase.Success(
                    delta = if (response.delta > 0) response.delta else 0,
                    newBalance = response.newBalance,
                ),
            )
        }
        // Refresh from server to get authoritative history list next time it's opened.
        load()
    }

    private fun handlePurchaseFailure(throwable: Throwable) {
        val iapError = (throwable as? IapManagerImpl.IapException)?.iapError
        val messageRes = when (iapError) {
            IapError.Cancelled -> R.string.wallet_iap_cancelled
            IapError.Network -> R.string.wallet_iap_network_error
            IapError.Pending -> R.string.wallet_iap_pending
            IapError.ItemAlreadyOwned -> R.string.wallet_iap_already_owned
            IapError.ServiceDisconnected -> R.string.wallet_iap_service_unavailable
            IapError.DeveloperError,
            is IapError.Unknown,
            is IapError.VerifyFailed,
            null -> R.string.wallet_iap_verify_failed
        }
        _snackbarMessages.tryEmit(UiText.StringResource(messageRes))
        _uiState.update {
            it.copy(
                iapPhase = if (iapError == IapError.ItemAlreadyOwned) it.iapPhase else IapPhase.Idle,
            )
        }
        // If Play says we own this already, run recovery so verify-then-consume can finish it.
        if (iapError == IapError.ItemAlreadyOwned) recoverPendingPurchases()
    }

    fun dismissIapPhase() {
        _uiState.update { it.copy(iapPhase = IapPhase.Idle) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}

