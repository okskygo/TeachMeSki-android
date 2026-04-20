package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.ChatMessage
import com.teachmeski.app.domain.model.ChatRoom
import com.teachmeski.app.domain.model.ChatRoomDetail
import com.teachmeski.app.domain.model.InboxRoomUpdate
import com.teachmeski.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChatRooms(offset: Int): Resource<Pair<List<ChatRoom>, Boolean>>
    suspend fun getChatRoomDetail(roomId: String): Resource<ChatRoomDetail>
    suspend fun getMessages(roomId: String): Resource<Pair<List<ChatMessage>, Boolean>>
    suspend fun getOlderMessages(roomId: String, beforeSentAt: String): Resource<Pair<List<ChatMessage>, Boolean>>
    suspend fun sendMessage(roomId: String, content: String): Resource<ChatMessage>
    suspend fun markRoomAsRead(roomId: String): Resource<Unit>
    suspend fun getUnreadCount(): Resource<Int>
    suspend fun createPathBChatRoom(instructorProfileId: String, lessonRequestId: String, firstMessage: String): Resource<String>
    suspend fun unlockPathBConversation(roomId: String, message: String): Resource<String>

    /** Broadcast channel `room:{roomId}`, event `new_message`. Collect in a coroutine; cancel to unsubscribe. */
    fun subscribeToRoomFlow(roomId: String): Flow<ChatMessage>

    /**
     * Broadcast channel `inbox:{auth_user_id}`, event `room_updated`.
     * Resolves the current user id internally. Emits when any chat_messages row
     * is inserted involving the current user. Collect in a coroutine; cancel to unsubscribe.
     */
    fun subscribeToInboxFlow(): Flow<InboxRoomUpdate>
}
