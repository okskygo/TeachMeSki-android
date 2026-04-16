package com.teachmeski.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactSubmissionDto(
    val name: String,
    val email: String,
    val message: String,
    @SerialName("user_id") val userId: String? = null,
)
