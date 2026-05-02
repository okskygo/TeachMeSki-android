package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.TokenTransaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenTransactionDto(
    val id: String,
    val amount: Int,
    val type: String,
    @SerialName("reference_type") val referenceType: String? = null,
    @SerialName("balance_after") val balanceAfter: Int,
    @SerialName("created_at") val createdAt: String,
)

fun TokenTransactionDto.toDomain() = TokenTransaction(
    id = id,
    amount = amount,
    type = type,
    referenceType = referenceType,
    balanceAfter = balanceAfter,
    createdAt = createdAt,
)
