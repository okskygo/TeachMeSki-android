package com.teachmeski.app.iap

/**
 * UI-ready product projection sourced from Play `ProductDetails` joined with the
 * `iap_products` catalog (token amounts are UI-only; the verify-purchase Edge Function
 * credits from its own authoritative mapping).
 *
 * `tokens` and `bonusTokens` are NEVER used to credit the wallet. They exist here solely
 * for card labels.
 */
data class IapProduct(
    /** Google Play productId, e.g. "tokens_starter". */
    val productId: String,
    /** Stable UI key: "starter" / "popular" / "pro" / "premium". */
    val tierKey: String,
    /** Localized formatted price from Play, e.g. "NT$150" or "¥500". */
    val formattedPrice: String,
    /** Raw price in micros from Play (1_000_000 = 1 unit of `currency`). */
    val priceMicros: Long,
    /** ISO 4217 currency code from Play, e.g. "TWD". */
    val currency: String,
    /** Base tokens advertised on the card (display only). */
    val tokens: Int,
    /** Bonus tokens advertised on the card (display only). */
    val bonusTokens: Int,
) {
    val totalTokens: Int get() = tokens + bonusTokens
}
