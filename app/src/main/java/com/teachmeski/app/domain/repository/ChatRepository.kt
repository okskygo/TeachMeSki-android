package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.ChatMessage
import com.teachmeski.app.domain.model.ChatRoom
import com.teachmeski.app.domain.model.ChatRoomDetail
import com.teachmeski.app.domain.model.InboxRoomUpdate
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /**
     * F-113 FR-113-001..004: list chat rooms scoped to the caller's currently
     * active panel. `activeRole` MUST drive the filter — never the
     * "user has an instructor profile" heuristic — so a `Both` user sees
     * student-side rooms in the student panel and instructor-side rooms in
     * the instructor panel.
     */
    suspend fun getChatRooms(activeRole: ActiveRole, offset: Int): Resource<Pair<List<ChatRoom>, Boolean>>
    suspend fun getChatRoomDetail(roomId: String): Resource<ChatRoomDetail>
    suspend fun getMessages(roomId: String): Resource<Pair<List<ChatMessage>, Boolean>>
    suspend fun getOlderMessages(roomId: String, beforeSentAt: String): Resource<Pair<List<ChatMessage>, Boolean>>
    suspend fun sendMessage(roomId: String, content: String): Resource<ChatMessage>
    suspend fun markRoomAsRead(roomId: String): Resource<Unit>

    /**
     * F-113 FR-113-007: unread count for the given panel only. Tab-bar badge
     * consumers should pass the current `activeRole`.
     */
    suspend fun getUnreadCount(activeRole: ActiveRole): Resource<Int>

    /**
     * F-113 FR-113-008: returns `(instructorPanel, studentPanel)` unread
     * counts in parallel for the app-icon badge sum. Returns `(0, studentCount)`
     * when the user has no instructor profile.
     */
    suspend fun getUnreadCountForBothPanels(): Resource<Pair<Int, Int>>

    suspend fun createPathBChatRoom(instructorProfileId: String, lessonRequestId: String, firstMessage: String): Resource<String>
    suspend fun unlockPathBConversation(roomId: String): Resource<String>

    /** Broadcast channel `room:{roomId}`, event `new_message`. Collect in a coroutine; cancel to unsubscribe. */
    fun subscribeToRoomFlow(roomId: String): Flow<ChatMessage>

    /**
     * Broadcast channel `inbox:{auth_user_id}`, event `room_updated`.
     * Resolves the current user id internally. Emits when any chat_messages row
     * is inserted involving the current user. Collect in a coroutine; cancel to unsubscribe.
     */
    fun subscribeToInboxFlow(): Flow<InboxRoomUpdate>
}
