package com.teachmeski.app.iap

/**
 * Errors surfaced by the IAP layer to the ViewModel.
 *
 * Mapped from BillingClient [com.android.billingclient.api.BillingResult] responseCodes
 * and from verify-purchase Edge Function HTTP errors. Keep this stable; UI messages
 * are driven by [mapIapErrorToMessage] (in the ui layer), not by debug strings here.
 */
sealed interface IapError {
    /** User tapped "Cancel" in the Google Play purchase sheet. */
    data object Cancelled : IapError

    /** Purchase is PENDING (slow payment method, parental approval). Not yet credited. */
    data object Pending : IapError

    /** Transient network / Play Store connection failure. */
    data object Network : IapError

    /** User already owns an unconsumed copy of the product. We recover via queryPendingPurchases. */
    data object ItemAlreadyOwned : IapError

    /** BillingClient rejected the request (bad product id, missing permission, etc.). */
    data object DeveloperError : IapError

    /** Lost connection to Google Play Billing service. */
    data object ServiceDisconnected : IapError

    /**
     * verify-purchase Edge Function returned a non-2xx or the RPC threw.
     * Caller should keep the Play purchaseToken and retry — the RPC is idempotent.
     */
    data class VerifyFailed(val httpStatus: Int, val code: String?) : IapError

    /** Anything we haven't explicitly modeled. [debug] is for Crashlytics, never shown to users. */
    data class Unknown(val debug: String) : IapError
}
