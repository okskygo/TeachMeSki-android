package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.User
import com.teachmeski.app.domain.model.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String = "student",
    @SerialName("deleted_at") val deletedAt: String? = null,
)

fun UserDto.toDomain() = User(
    id = id,
    displayName = displayName,
    avatarUrl = avatarUrl,
    role = UserRole.fromString(role),
    deletedAt = deletedAt,
)
