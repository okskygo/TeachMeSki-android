package com.teachmeski.app.ui.wallet

/**
 * UI-layer snapshot of where a purchase is in its lifecycle.
 *
 * Transitions (happy path):
 *   Idle -> Purchasing(productId) -> Verifying -> Success
 *   Idle -> Purchasing(productId) -> Verifying -> WebhookDelayed (rare; recoverable)
 *
 * Error branches land back at [Idle] after a snackbar, except [Success] which
 * is shown as a full-screen overlay with its own auto-dismiss.
 */
sealed interface IapPhase {
    data object Idle : IapPhase

    /** User tapped a package; we've called launchBillingFlow. */
    data class Purchasing(val productId: String) : IapPhase

    /** Play returned OK with a Purchase; we're calling verify-purchase now. */
    data class Verifying(val productId: String) : IapPhase

    /** verify-purchase returned credited=true. Show overlay for [delta] tokens. */
    data class Success(val delta: Int, val newBalance: Int) : IapPhase

    /** Play returned OK but verify-purchase failed. Stored token so we can retry in onStart. */
    data object WebhookDelayed : IapPhase
}
