package com.teachmeski.app.domain.model

data class InstructorPreview(
    val instructorId: String,
    val userId: String?,
    val displayName: String?,
    val avatarUrl: String?,
    val hasUnread: Boolean,
    val roomId: String?,
    val isReviewed: Boolean = false,
    val ratingAvg: Double? = null,
    val ratingCount: Int = 0,
    val phoneVerifiedAt: String? = null,
    val shortId: String? = null,
)
