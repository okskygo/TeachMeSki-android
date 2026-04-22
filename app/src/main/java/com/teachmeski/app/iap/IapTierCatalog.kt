package com.teachmeski.app.iap

/**
 * Static metadata per tier. Paired with Play `ProductDetails` at runtime to build [IapProduct].
 *
 * WARNING: `tokens` and `bonusTokens` here are **display-only**. The authoritative map
 * lives in `supabase/functions/verify-purchase/config.ts` (PRODUCT_TOKEN_MAP) and must be
 * kept in sync manually. If they drift, the UI will show the client value but the server
 * will credit the server value (and win). When adding/editing a tier, update both.
 */
internal data class IapTier(
    val productId: String,
    val tierKey: String,
    val tokens: Int,
    val bonusTokens: Int,
)

internal object IapTierCatalog {
    val tiers: List<IapTier> = listOf(
        IapTier(productId = "tokens_starter", tierKey = "starter", tokens = 500, bonusTokens = 0),
        IapTier(productId = "tokens_popular", tierKey = "popular", tokens = 1_000, bonusTokens = 50),
        IapTier(productId = "tokens_pro", tierKey = "pro", tokens = 2_500, bonusTokens = 250),
        IapTier(productId = "tokens_premium", tierKey = "premium", tokens = 5_000, bonusTokens = 750),
    )

    val productIds: List<String> = tiers.map { it.productId }

    fun byProductId(id: String): IapTier? = tiers.firstOrNull { it.productId == id }
}
