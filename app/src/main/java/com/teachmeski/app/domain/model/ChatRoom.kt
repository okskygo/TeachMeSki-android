package com.teachmeski.app.domain.model

data class ChatRoom(
    val id: String,
    val lessonRequestId: String,
    val instructorId: String,
    val userId: String,
    val otherPartyName: String,
    val otherPartyAvatarUrl: String?,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val lastMessageSenderId: String?,
    val unreadCount: Int,
    val discipline: String?,
)
