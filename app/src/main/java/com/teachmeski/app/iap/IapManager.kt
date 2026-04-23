package com.teachmeski.app.iap

import android.app.Activity
import android.util.Log
import com.teachmeski.app.data.model.IapProductDto
import com.teachmeski.app.data.remote.IapProductsDataSource
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * High-level IAP surface exposed to the ViewModel.
 *
 * Responsibilities:
 *  - Product catalog (Play ProductDetails + `iap_products` rows → [IapProduct])
 *  - Launching the purchase flow for an Activity
 *  - Querying purchases still owned (recovery after crash / delayed credit)
 *  - Consuming a purchase (ONLY after verify-purchase credited us)
 *
 * Intentionally does NOT talk to Supabase. Verification is done by [VerifyPurchaseClient].
 */
interface IapManager {
    /** Query Play for all tier productIds; merge with catalog; return UI-ready products. */
    suspend fun loadProducts(): Result<List<IapProduct>>

    /**
     * Launches the Play purchase UI. Suspends until [PurchasesUpdatedListener] emits
     * a result for this productId. Returns the first matching [Purchase] on OK,
     * else an [IapError]-mapped failure.
     */
    suspend fun launchPurchase(activity: Activity, productId: String): Result<Purchase>

    /** Consume (finalize) a purchase token after the backend has credited tokens. */
    suspend fun consume(purchaseToken: String): Result<Unit>

    /** Returns all in-app purchases the user currently owns (PURCHASED + PENDING). */
    suspend fun queryPendingPurchases(): List<Purchase>
}

@Singleton
class IapManagerImpl @Inject constructor(
    private val provider: BillingClientProvider,
    private val iapProductsDataSource: IapProductsDataSource,
) : IapManager {

    /** Cache of ProductDetails keyed by productId. Needed by [launchPurchase]. */
    private val productDetailsCache = mutableMapOf<String, ProductDetails>()

    override suspend fun loadProducts(): Result<List<IapProduct>> = runCatching {
        val dtos: List<IapProductDto> = runCatching { iapProductsDataSource.listActive() }
            .getOrElse { e ->
                Log.e(TAG, "listActive iap_products failed", e)
                emptyList()
            }
        if (dtos.isEmpty()) return@runCatching emptyList()

        val dtoByProductId = dtos.associateBy { it.productId }
        val client = provider.ensureConnected()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                dtos.map { dto ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(dto.productId)
                        .setProductType(ProductType.INAPP)
                        .build()
                },
            )
            .build()
        val result = client.queryProductDetails(params)
        val billingResult = result.billingResult
        if (billingResult.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
            error("queryProductDetails failed code=${billingResult.responseCode} msg=${billingResult.debugMessage}")
        }
        val details = result.productDetailsList.orEmpty()
        details.forEach { productDetailsCache[it.productId] = it }
        details.mapNotNull { pd ->
            val dto = dtoByProductId[pd.productId] ?: return@mapNotNull null
            val offer = pd.oneTimePurchaseOfferDetails ?: return@mapNotNull null
            IapProduct(
                productId = pd.productId,
                tierKey = dto.tierKey,
                formattedPrice = offer.formattedPrice,
                priceMicros = offer.priceAmountMicros,
                currency = offer.priceCurrencyCode,
                tokens = dto.tokens,
                bonusTokens = dto.bonusTokens,
            )
        }
            .sortedBy { dtoByProductId[it.productId]?.sortOrder ?: Int.MAX_VALUE }
    }

    override suspend fun launchPurchase(
        activity: Activity,
        productId: String,
    ): Result<Purchase> = runCatching {
        val client = provider.ensureConnected()
        val pd = productDetailsCache[productId]
            ?: error("ProductDetails for $productId not loaded; call loadProducts() first")
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(pd)
                        .build(),
                ),
            )
            .build()
        val launch = client.launchBillingFlow(activity, flowParams)
        if (launch.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
            error("launchBillingFlow code=${launch.responseCode} msg=${launch.debugMessage}")
        }
        // Await the purchase update that matches this productId.
        awaitPurchaseFor(productId).getOrThrow()
    }

    override suspend fun consume(purchaseToken: String): Result<Unit> = runCatching {
        val client = provider.ensureConnected()
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        val result = client.consumePurchase(params)
        val code = result.billingResult.responseCode
        if (code != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
            error("consumePurchase code=$code msg=${result.billingResult.debugMessage}")
        }
    }

    override suspend fun queryPendingPurchases(): List<Purchase> {
        val client = provider.ensureConnected()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.INAPP)
            .build()
        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
            return emptyList()
        }
        return result.purchasesList
    }

    private suspend fun awaitPurchaseFor(productId: String): Result<Purchase> {
        return provider.purchaseUpdates
            .filter { update -> updateMatches(update, productId) }
            .map { update ->
                val br = update.billingResult
                when (br.responseCode) {
                    com.android.billingclient.api.BillingClient.BillingResponseCode.OK -> {
                        val matching = update.purchases.firstOrNull { it.products.contains(productId) }
                        if (matching != null) {
                            Result.success(matching)
                        } else {
                            Result.failure(IllegalStateException("OK but no matching purchase for $productId"))
                        }
                    }

                    com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED ->
                        Result.failure(IapException(IapError.Cancelled))

                    com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                        Result.failure(IapException(IapError.ItemAlreadyOwned))

                    com.android.billingclient.api.BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                        Result.failure(IapException(IapError.DeveloperError))

                    com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                        Result.failure(IapException(IapError.ServiceDisconnected))

                    com.android.billingclient.api.BillingClient.BillingResponseCode.NETWORK_ERROR ->
                        Result.failure(IapException(IapError.Network))

                    else -> Result.failure(
                        IapException(
                            IapError.Unknown(
                                "code=${br.responseCode} msg=${br.debugMessage}",
                            ),
                        ),
                    )
                }
            }
            .first()
    }

    private fun updateMatches(
        update: BillingClientProvider.PurchaseUpdate,
        productId: String,
    ): Boolean {
        val br: BillingResult = update.billingResult
        // Non-OK results can arrive without purchases; treat as matching so caller sees the error.
        if (br.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) return true
        return update.purchases.any { it.products.contains(productId) }
    }

    /** Internal carrier so we can propagate [IapError] through Result. */
    class IapException(val iapError: IapError) : RuntimeException(iapError.toString())

    private companion object {
        private const val TAG = "IapManager"
    }
}

