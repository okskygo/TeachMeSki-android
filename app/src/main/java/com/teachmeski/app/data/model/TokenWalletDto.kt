package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.TokenWallet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenWalletDto(
    val balance: Int = 0,
    @SerialName("instructor_id") val instructorId: String = "",
)

fun TokenWalletDto.toDomain() = TokenWallet(
    balance = balance,
    instructorId = instructorId,
)
