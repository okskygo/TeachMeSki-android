package com.teachmeski.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomDto(
    val id: String,
    @SerialName("lesson_request_id") val lessonRequestId: String,
    @SerialName("instructor_id") val instructorId: String,
    @SerialName("user_id") val userId: String,
    val status: String = "active",
    @SerialName("last_message_content") val lastMessageContent: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("last_message_sender_id") val lastMessageSenderId: String? = null,
    @SerialName("last_read_at") val lastReadAt: String? = null,
    @SerialName("instructor_last_read_at") val instructorLastReadAt: String? = null,
)
