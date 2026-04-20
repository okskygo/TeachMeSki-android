package com.teachmeski.app.domain.model

/**
 * Realtime broadcast payload for topic `inbox:{auth_user_id}`, event `room_updated`.
 * Emitted by the `on_chat_message_broadcast` Postgres trigger after every
 * `chat_messages` INSERT.
 */
data class InboxRoomUpdate(
    val roomId: String,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val senderId: String?,
)
