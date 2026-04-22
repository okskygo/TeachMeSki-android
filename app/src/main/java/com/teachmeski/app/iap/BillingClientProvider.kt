package com.teachmeski.app.iap

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the single process-wide [BillingClient] + its connection lifecycle.
 *
 * Exposes [purchaseUpdates] as the PurchasesUpdatedListener -> Flow bridge.
 * Consumers must call [ensureConnected] before any BillingClient call.
 *
 * NOTE: `enablePendingPurchases()` in Play Billing 8.x requires a [PendingPurchasesParams]
 * argument; we opt in to one-time-product pending purchases, which is mandatory for consumables.
 */
@Singleton
class BillingClientProvider @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>(
        replay = 0,
        extraBufferCapacity = 16,
    )
    val purchaseUpdates: SharedFlow<PurchaseUpdate> = _purchaseUpdates.asSharedFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        _purchaseUpdates.tryEmit(PurchaseUpdate(billingResult, purchases.orEmpty()))
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    private val connectMutex = Mutex()

    /**
     * Ensures the BillingClient is connected; safe to call repeatedly.
     * If already READY, returns immediately. Otherwise starts the connection
     * and suspends until either setup finishes or fails.
     */
    suspend fun ensureConnected(): BillingClient = connectMutex.withLock {
        if (billingClient.isReady) return@withLock billingClient
        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (cont.isActive) cont.resume(billingClient)
                }

                override fun onBillingServiceDisconnected() {
                    // enableAutoServiceReconnection() handles retry; no-op here.
                }
            })
        }
    }

    /** Raw signal from [PurchasesUpdatedListener]. */
    data class PurchaseUpdate(
        val billingResult: BillingResult,
        val purchases: List<Purchase>,
    )
}
