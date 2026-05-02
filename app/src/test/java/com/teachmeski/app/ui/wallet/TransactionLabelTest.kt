package com.teachmeski.app.ui.wallet

import com.teachmeski.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * F-008 P3 — `resolveTransactionLabelRes` precedence:
 *   1. `referenceType == "unlock_auto_refund"` wins regardless of `type`.
 *   2. Otherwise the legacy `type`-only mapping is preserved.
 */
class TransactionLabelTest {

    @Test
    fun `unlock_auto_refund reference_type wins over refund type`() {
        assertEquals(
            R.string.wallet_transaction_unlock_auto_refund,
            resolveTransactionLabelRes("refund", "unlock_auto_refund"),
        )
    }

    @Test
    fun `null reference_type falls back to refund type label`() {
        assertEquals(
            R.string.wallet_transaction_refund,
            resolveTransactionLabelRes("refund", null),
        )
    }

    @Test
    fun `unknown reference_type falls back to refund type label`() {
        assertEquals(
            R.string.wallet_transaction_refund,
            resolveTransactionLabelRes("refund", "future_unknown_reference"),
        )
    }

    @Test
    fun `purchase type maps to purchase label`() {
        assertEquals(
            R.string.wallet_transaction_purchase,
            resolveTransactionLabelRes("purchase", null),
        )
    }

    @Test
    fun `bonus type maps to bonus label`() {
        assertEquals(
            R.string.wallet_transaction_bonus,
            resolveTransactionLabelRes("bonus", null),
        )
    }

    @Test
    fun `unlock type maps to unlock label`() {
        assertEquals(
            R.string.wallet_transaction_unlock,
            resolveTransactionLabelRes("unlock", null),
        )
    }

    @Test
    fun `compensation type maps to compensation label`() {
        assertEquals(
            R.string.wallet_transaction_compensation,
            resolveTransactionLabelRes("compensation", null),
        )
    }

    @Test
    fun `unknown type falls back to other label`() {
        assertEquals(
            R.string.wallet_transaction_other,
            resolveTransactionLabelRes("WeIrD_TyPe", null),
        )
    }

    @Test
    fun `type matching is case insensitive`() {
        assertEquals(
            R.string.wallet_transaction_purchase,
            resolveTransactionLabelRes("PURCHASE", null),
        )
    }
}
