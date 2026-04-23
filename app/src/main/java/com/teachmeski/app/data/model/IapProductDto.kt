package com.teachmeski.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IapProductDto(
    @SerialName("product_id") val productId: String,
    @SerialName("tier_key") val tierKey: String,
    val tokens: Int,
    @SerialName("bonus_tokens") val bonusTokens: Int,
    @SerialName("sort_order") val sortOrder: Int,
    @SerialName("is_active") val isActive: Boolean,
)
