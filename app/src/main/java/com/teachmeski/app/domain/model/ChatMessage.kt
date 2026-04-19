package com.teachmeski.app.domain.model

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val content: String,
    val sentAt: String,
    val isOptimistic: Boolean = false,
    val isFailed: Boolean = false,
)
