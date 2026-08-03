package com.teachmeski.app.ui.wallet

import androidx.annotation.StringRes
import com.teachmeski.app.R
import java.util.Locale

/**
 * F-008 P3 — `token_transactions` row → string resource for the wallet
 * "Credit history" list.
 *
 * Precedence:
 *   1. `referenceType == "unlock_auto_refund"` → dedicated label
 *      ("自動退款（學員未回覆）" / "Auto refund (no reply from student)").
 *   2. `referenceType == "referral_bonus"` → dedicated label
 *      ("推薦獎勵" / "Referral bonus") — F-116 referral payouts share the
 *      generic `bonus` type with the welcome bonus, so they need their own
 *      label to avoid being shown as "迎新贈送" (Welcome Bonus).
 *   3. Otherwise fall back to the generic `type` mapping that has shipped
 *      since the wallet feature launched (`unlock`, `purchase`, `bonus`,
 *      `compensation`, `refund`, default `other`).
 *
 * Type matching is case-insensitive; unknown types fall through to
 * `wallet_transaction_other`.
 */
@StringRes
fun resolveTransactionLabelRes(type: String, referenceType: String?): Int {
    if (referenceType == "unlock_auto_refund") {
        return R.string.wallet_transaction_unlock_auto_refund
    }
    if (referenceType == "referral_bonus") {
        return R.string.wallet_transaction_referral_bonus
    }
    return when (type.lowercase(Locale.US)) {
        "unlock" -> R.string.wallet_transaction_unlock
        "purchase" -> R.string.wallet_transaction_purchase
        "bonus" -> R.string.wallet_transaction_bonus
        "compensation" -> R.string.wallet_transaction_compensation
        "refund" -> R.string.wallet_transaction_refund
        else -> R.string.wallet_transaction_other
    }
}
