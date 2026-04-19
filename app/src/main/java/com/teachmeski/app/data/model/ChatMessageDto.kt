package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.ChatMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("sender_id") val senderId: String,
    val content: String,
    @SerialName("sent_at") val sentAt: String,
)

fun ChatMessageDto.toDomain(): ChatMessage = ChatMessage(
    id = id,
    roomId = roomId,
    senderId = senderId,
    content = content,
    sentAt = sentAt,
)
