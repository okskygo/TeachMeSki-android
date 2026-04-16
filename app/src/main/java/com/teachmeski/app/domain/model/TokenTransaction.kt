package com.teachmeski.app.domain.model

data class TokenTransaction(
    val id: String,
    val amount: Int,
    val type: String,
    val balanceAfter: Int,
    val createdAt: String,
)
