package com.teachmeski.app.domain.model

enum class InstructorSection { UserInitiated, ExpertInitiated }

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
    /** F-108: non-null = LINE identity verified. */
    val lineUserId: String? = null,
    val shortId: String? = null,
    val section: InstructorSection = InstructorSection.UserInitiated,
    val lastMessageContent: String? = null,
    val lastMessageAt: String? = null,
)
