package com.teachmeski.app.domain.model

data class UnlockedRoom(
    val roomId: String,
    val lessonRequestId: String,
    val userId: String,
    val studentDisplayName: String,
    val studentAvatarUrl: String?,
    val discipline: Discipline,
    val skillLevel: Int?,
    val groupSize: Int,
    val dateStart: String?,
    val dateEnd: String?,
    val datesFlexible: Boolean,
    val lastMessageContent: String?,
    val lastMessageAt: String?,
    val hasUnread: Boolean,
)
