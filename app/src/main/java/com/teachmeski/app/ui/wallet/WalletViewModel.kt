package com.teachmeski.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.TokenWallet
import com.teachmeski.app.domain.repository.WalletRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val wallet: TokenWallet? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = walletRepository.getWallet()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            wallet = result.data,
                            isLoading = false,
                            error = null,
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { s -> s.copy(isLoading = true) }
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}
