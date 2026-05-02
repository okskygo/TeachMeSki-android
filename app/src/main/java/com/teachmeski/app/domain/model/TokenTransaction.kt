package com.teachmeski.app.domain.model

data class TokenTransaction(
    val id: String,
    val amount: Int,
    val type: String,
    /**
     * F-008 P3: distinguishes the source of `type='refund'` rows.
     * `'unlock_auto_refund'` = 48hr no-reply daily-cron refund (own label).
     * `null` or other values fall back to the generic `type` label.
     */
    val referenceType: String?,
    val balanceAfter: Int,
    val createdAt: String,
)
