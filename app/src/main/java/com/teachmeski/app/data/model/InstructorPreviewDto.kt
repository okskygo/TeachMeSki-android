package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.InstructorSection

fun ChatRoomPreviewDto.toInstructorPreview(
    currentUserId: String,
    section: InstructorSection = InstructorSection.UserInitiated,
): InstructorPreview {
    val profile = instructorProfile
    val hasUnread =
        lastMessageAt != null &&
            lastMessageSenderId != currentUserId &&
            (lastReadAt == null || lastMessageAt > lastReadAt)

    return InstructorPreview(
        instructorId = instructorId,
        userId = profile?.userId,
        displayName = profile?.displayName,
        avatarUrl = profile?.avatarUrl,
        hasUnread = hasUnread,
        roomId = id,
        ratingAvg = profile?.ratingAvg,
        ratingCount = profile?.ratingCount ?: 0,
        phoneVerifiedAt = profile?.phoneVerifiedAt,
        shortId = profile?.shortId,
        section = section,
        lastMessageContent = lastMessageContent,
        lastMessageAt = lastMessageAt,
    )
}
