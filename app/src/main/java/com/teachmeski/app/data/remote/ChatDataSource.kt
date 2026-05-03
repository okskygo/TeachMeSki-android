package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.BlockCheckRow
import com.teachmeski.app.data.model.CertPrefRow
import com.teachmeski.app.data.model.ChatMessageDto
import com.teachmeski.app.data.model.ChatRoomDto
import com.teachmeski.app.data.model.InstructorProfileDetailDto
import com.teachmeski.app.data.model.LessonRequestDetailDto
import com.teachmeski.app.data.model.ReviewCheckRow
import com.teachmeski.app.data.model.UnlockCheckRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val MESSAGES_PAGE_SIZE = 50
private const val ROOMS_PAGE_SIZE = 20

@Singleton
class ChatDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    @Serializable
    data class UserNameRow(
        val id: String,
        @SerialName("display_name") val displayName: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
    )

    @Serializable
    data class ResortNameRow(
        val id: String,
        @SerialName("name_zh") val nameZh: String,
        @SerialName("name_en") val nameEn: String,
    )

    @Serializable
    data class TokenWalletRow(
        val balance: Int,
    )

    // --- Room list queries ---

    suspend fun getChatRoomsForSeeker(userId: String, offset: Int, limit: Int = ROOMS_PAGE_SIZE): List<ChatRoomDto> =
        supabaseClient.postgrest.from("chat_rooms")
            .select {
                filter { eq("user_id", userId) }
                order("last_message_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<ChatRoomDto>()

    suspend fun getChatRoomsForInstructor(instructorProfileId: String, offset: Int, limit: Int = ROOMS_PAGE_SIZE): List<ChatRoomDto> =
        supabaseClient.postgrest.from("chat_rooms")
            .select {
                filter { eq("instructor_id", instructorProfileId) }
                order("last_message_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<ChatRoomDto>()

    suspend fun getInstructorProfileId(userId: String): String? {
        @Serializable
        data class IdRow(val id: String)
        return supabaseClient.postgrest.from("instructor_profiles")
            .select(columns = Columns.list("id")) {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<IdRow>()?.id
    }

    suspend fun getInstructorUserIds(instructorProfileIds: List<String>): Map<String, String> {
        if (instructorProfileIds.isEmpty()) return emptyMap()
        @Serializable
        data class Row(val id: String, @SerialName("user_id") val userId: String)
        return supabaseClient.postgrest.from("instructor_profiles")
            .select(columns = Columns.raw("id, user_id")) {
                filter { isIn("id", instructorProfileIds) }
            }
            .decodeList<Row>()
            .associate { it.id to it.userId }
    }

    suspend fun getInstructorDisplayInfo(instructorProfileIds: List<String>): Map<String, Pair<String?, String?>> {
        if (instructorProfileIds.isEmpty()) return emptyMap()
        @Serializable
        data class Row(
            val id: String,
            @SerialName("display_name") val displayName: String? = null,
            @SerialName("avatar_url") val avatarUrl: String? = null,
        )
        return supabaseClient.postgrest.from("instructor_profiles")
            .select(columns = Columns.raw("id, display_name, avatar_url")) {
                filter { isIn("id", instructorProfileIds) }
            }
            .decodeList<Row>()
            .associate { it.id to Pair(it.displayName, it.avatarUrl) }
    }

    suspend fun getUserDisplayInfo(userIds: List<String>): Map<String, Pair<String?, String?>> {
        if (userIds.isEmpty()) return emptyMap()
        return supabaseClient.postgrest.from("users")
            .select(columns = Columns.raw("id, display_name, avatar_url")) {
                filter { isIn("id", userIds) }
            }
            .decodeList<UserNameRow>()
            .associate { it.id to Pair(it.displayName, it.avatarUrl) }
    }

    suspend fun getLessonRequestDisciplines(requestIds: List<String>): Map<String, String> {
        if (requestIds.isEmpty()) return emptyMap()
        @Serializable
        data class Row(val id: String, val discipline: String)
        return supabaseClient.postgrest.from("lesson_requests")
            .select(columns = Columns.raw("id, discipline")) {
                filter { isIn("id", requestIds) }
            }
            .decodeList<Row>()
            .associate { it.id to it.discipline }
    }

    // --- Messages ---

    suspend fun getChatMessages(roomId: String, limit: Int = MESSAGES_PAGE_SIZE): List<ChatMessageDto> =
        supabaseClient.postgrest.from("chat_messages")
            .select {
                filter { eq("room_id", roomId) }
                order("sent_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ChatMessageDto>()
            .reversed()

    suspend fun getOlderMessages(roomId: String, beforeSentAt: String, limit: Int = MESSAGES_PAGE_SIZE): List<ChatMessageDto> =
        supabaseClient.postgrest.from("chat_messages")
            .select {
                filter {
                    eq("room_id", roomId)
                    lt("sent_at", beforeSentAt)
                }
                order("sent_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ChatMessageDto>()
            .reversed()

    suspend fun insertMessage(roomId: String, senderId: String, content: String, sentAt: String): ChatMessageDto =
        supabaseClient.postgrest.from("chat_messages")
            .insert(
                buildJsonObject {
                    put("room_id", roomId)
                    put("sender_id", senderId)
                    put("content", content)
                    put("sent_at", sentAt)
                },
            ) { select() }
            .decodeSingle<ChatMessageDto>()

    // --- Mark as read ---

    suspend fun markRoomAsReadForSeeker(roomId: String, userId: String) {
        supabaseClient.postgrest.from("chat_rooms")
            .update(
                buildJsonObject { put("last_read_at", Instant.now().toString()) },
            ) {
                filter {
                    eq("id", roomId)
                    eq("user_id", userId)
                }
            }
    }

    suspend fun markRoomAsReadForInstructor(roomId: String, instructorProfileId: String) {
        supabaseClient.postgrest.from("chat_rooms")
            .update(
                buildJsonObject { put("instructor_last_read_at", Instant.now().toString()) },
            ) {
                filter {
                    eq("id", roomId)
                    eq("instructor_id", instructorProfileId)
                }
            }
    }

    // --- Room detail queries ---

    suspend fun getChatRoom(roomId: String): ChatRoomDto =
        supabaseClient.postgrest.from("chat_rooms")
            .select { filter { eq("id", roomId) } }
            .decodeSingle<ChatRoomDto>()

    suspend fun getLessonRequestDetail(requestId: String): LessonRequestDetailDto =
        supabaseClient.postgrest.from("lesson_requests")
            .select(
                columns = Columns.raw(
                    "id, discipline, skill_level, group_size, has_children, duration_days, date_start, date_end, dates_flexible, languages, additional_notes, all_regions_selected, resort_ids, equipment_rental, needs_transport, transport_note",
                ),
            ) { filter { eq("id", requestId) } }
            .decodeSingle<LessonRequestDetailDto>()

    suspend fun getCertPrefs(requestId: String): List<CertPrefRow> =
        supabaseClient.postgrest.from("lesson_request_cert_prefs")
            .select(columns = Columns.list("certification_code")) {
                filter { eq("lesson_request_id", requestId) }
            }
            .decodeList<CertPrefRow>()

    suspend fun getResortNames(resortIds: List<String>): List<ResortNameRow> {
        if (resortIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("ski_resorts")
            .select(columns = Columns.raw("id, name_zh, name_en")) {
                filter { isIn("id", resortIds) }
            }
            .decodeList<ResortNameRow>()
    }

    suspend fun getInstructorProfileDetail(instructorProfileId: String): InstructorProfileDetailDto =
        supabaseClient.postgrest.from("instructor_profiles")
            .select(
                columns = Columns.raw(
                    "id, user_id, short_id, display_name, avatar_url, rating_avg, rating_count, bio, line_user_id",
                ),
            ) { filter { eq("id", instructorProfileId) } }
            .decodeSingle<InstructorProfileDetailDto>()

    suspend fun getUserProfile(userId: String): UserNameRow =
        supabaseClient.postgrest.from("users")
            .select(columns = Columns.raw("id, display_name, avatar_url")) {
                filter { eq("id", userId) }
            }
            .decodeSingle<UserNameRow>()

    suspend fun checkIsReviewed(reviewerId: String, instructorId: String): Boolean =
        supabaseClient.postgrest.from("reviews")
            .select(columns = Columns.list("instructor_id")) {
                filter {
                    eq("reviewer_id", reviewerId)
                    eq("instructor_id", instructorId)
                }
            }
            .decodeList<ReviewCheckRow>()
            .isNotEmpty()

    suspend fun checkIsBlocked(userId: String, otherUserId: String): Boolean {
        val blocks = supabaseClient.postgrest.from("blocks")
            .select(columns = Columns.raw("blocker_id, blocked_id")) {
                filter {
                    or {
                        and {
                            eq("blocker_id", userId)
                            eq("blocked_id", otherUserId)
                        }
                        and {
                            eq("blocker_id", otherUserId)
                            eq("blocked_id", userId)
                        }
                    }
                }
            }
            .decodeList<BlockCheckRow>()
        return blocks.isNotEmpty()
    }

    /**
     * F-008 P3: only an `'active'` `request_unlocks` row counts as
     * "currently unlocked". A `'refunded'` (auto-refunded by daily cron) or
     * `'completed'` row falls through so the instructor sees `ChatUnlockBar`
     * and can re-unlock. Without this filter, refunded rooms would
     * incorrectly remain on `ChatInput`.
     */
    suspend fun checkHasUnlock(instructorId: String, lessonRequestId: String): Boolean =
        supabaseClient.postgrest.from("request_unlocks")
            .select(columns = Columns.raw("instructor_id, lesson_request_id")) {
                filter {
                    eq("instructor_id", instructorId)
                    eq("lesson_request_id", lessonRequestId)
                    eq("status", "active")
                }
            }
            .decodeList<UnlockCheckRow>()
            .isNotEmpty()

    suspend fun checkHasSentMessage(roomId: String, senderId: String): Boolean {
        @Serializable
        data class MessageIdRow(val id: String)
        return supabaseClient.postgrest.from("chat_messages")
            .select(columns = Columns.list("id")) {
                filter {
                    eq("room_id", roomId)
                    eq("sender_id", senderId)
                }
                limit(1)
            }
            .decodeList<MessageIdRow>()
            .isNotEmpty()
    }

    /**
     * Review eligibility: both student and instructor must have sent at least
     * one message in the room (applies to Path-A and Path-B). Returns true
     * only when [studentUserId] AND [instructorUserId] each have ≥1 message
     * in [roomId].
     */
    suspend fun checkBothPartiesMessaged(
        roomId: String,
        studentUserId: String,
        instructorUserId: String,
    ): Boolean {
        if (studentUserId.isBlank() || instructorUserId.isBlank()) return false
        return checkHasSentMessage(roomId, studentUserId) &&
            checkHasSentMessage(roomId, instructorUserId)
    }

    suspend fun getWalletBalance(instructorProfileId: String): Int =
        supabaseClient.postgrest.from("token_wallets")
            .select(columns = Columns.list("balance")) {
                filter { eq("instructor_id", instructorProfileId) }
            }
            .decodeSingleOrNull<TokenWalletRow>()?.balance ?: 0

    // --- Unread count ---

    suspend fun getUnreadCountForSeeker(userId: String): Int {
        val rooms = supabaseClient.postgrest.from("chat_rooms")
            .select(columns = Columns.raw("id, lesson_request_id, instructor_id, user_id, status, last_message_content, last_message_at, last_message_sender_id, last_read_at, instructor_last_read_at")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<ChatRoomDto>()
        return rooms.count { room ->
            !room.lastMessageAt.isNullOrBlank() &&
                !room.lastMessageSenderId.isNullOrBlank() &&
                room.lastMessageSenderId != userId &&
                (room.lastReadAt.isNullOrBlank() || room.lastMessageAt > room.lastReadAt)
        }
    }

    suspend fun getUnreadCountForInstructor(instructorProfileId: String, instructorUserId: String): Int {
        val rooms = supabaseClient.postgrest.from("chat_rooms")
            .select(columns = Columns.raw("id, lesson_request_id, instructor_id, user_id, status, last_message_content, last_message_at, last_message_sender_id, last_read_at, instructor_last_read_at")) {
                filter { eq("instructor_id", instructorProfileId) }
            }
            .decodeList<ChatRoomDto>()
        return rooms.count { room ->
            !room.lastMessageAt.isNullOrBlank() &&
                !room.lastMessageSenderId.isNullOrBlank() &&
                room.lastMessageSenderId != instructorUserId &&
                (room.instructorLastReadAt.isNullOrBlank() || room.lastMessageAt > room.instructorLastReadAt)
        }
    }

    // --- Path-B ---

    suspend fun createPathBChatRoom(
        instructorProfileId: String,
        lessonRequestId: String,
        studentUserId: String,
        firstMessage: String,
    ): String {
        @Serializable
        data class ExistingRoomRow(val id: String)
        val existingRoom = supabaseClient.postgrest.from("chat_rooms")
            .select(columns = Columns.list("id")) {
                filter {
                    eq("instructor_id", instructorProfileId)
                    eq("lesson_request_id", lessonRequestId)
                }
            }
            .decodeSingleOrNull<ExistingRoomRow>()

        if (existingRoom != null) return existingRoom.id

        @Serializable
        data class NewRoom(val id: String)
        val room = supabaseClient.postgrest.from("chat_rooms")
            .insert(
                buildJsonObject {
                    put("lesson_request_id", lessonRequestId)
                    put("instructor_id", instructorProfileId)
                    put("user_id", studentUserId)
                },
            ) { select() }
            .decodeSingle<NewRoom>()

        insertMessage(room.id, studentUserId, firstMessage, Instant.now().toString())
        return room.id
    }

    suspend fun executeUnlock(
        authUserId: String,
        instructorProfileId: String,
        lessonRequestId: String,
        instructorMessage: String,
    ): Map<String, Any?> {
        val params = buildJsonObject {
            put("p_auth_user_id", authUserId)
            put("p_instructor_profile_id", instructorProfileId)
            put("p_lesson_request_id", lessonRequestId)
            put("p_instructor_message", instructorMessage)
        }
        val result = supabaseClient.postgrest.rpc("execute_unlock", params)
        return result.decodeAs<JsonObject>().toAnyValueMap()
    }

    // --- Broadcast helpers ---

    fun getRealtimeChannel(channelId: String) = supabaseClient.realtime.channel(channelId)

    /**
     * Subscribes to Realtime topic `room:{roomId}` broadcast event [NEW_MESSAGE_EVENT].
     * Matches web `ChatMessages.tsx` subscription.
     */
    fun roomNewMessagesFlow(roomId: String): Flow<ChatMessageDto> = channelFlow {
        val topic = "room:$roomId"
        val channel = supabaseClient.realtime.channel(topic)
        val collectJob = launch {
            channel.broadcastFlow<ChatMessageDto>(NEW_MESSAGE_EVENT).collect { dto ->
                send(dto)
            }
        }
        try {
            channel.subscribe(blockUntilSubscribed = true)
        } catch (e: Exception) {
            collectJob.cancel()
            close(e)
            return@channelFlow
        }
        awaitClose {
            collectJob.cancel()
            runBlocking {
                try {
                    channel.unsubscribe()
                } catch (_: Exception) {
                }
            }
        }
    }

    /**
     * Subscribes to Realtime topic `inbox:{userId}` broadcast event [ROOM_UPDATED_EVENT].
     * Matches web `ChatRoomList.tsx` subscription. Payload shape:
     * { room_id, last_message, last_message_at, sender_id }
     */
    fun inboxUpdatesFlow(userId: String): Flow<InboxRoomUpdateDto> = channelFlow {
        val topic = "inbox:$userId"
        val channel = supabaseClient.realtime.channel(topic)
        val collectJob = launch {
            channel.broadcastFlow<InboxRoomUpdateDto>(ROOM_UPDATED_EVENT).collect { dto ->
                send(dto)
            }
        }
        try {
            channel.subscribe(blockUntilSubscribed = true)
        } catch (e: Exception) {
            collectJob.cancel()
            close(e)
            return@channelFlow
        }
        awaitClose {
            collectJob.cancel()
            runBlocking {
                try {
                    channel.unsubscribe()
                } catch (_: Exception) {
                }
            }
        }
    }

    @Serializable
    data class InboxRoomUpdateDto(
        @SerialName("room_id") val roomId: String,
        @SerialName("last_message") val lastMessage: String? = null,
        @SerialName("last_message_at") val lastMessageAt: String? = null,
        @SerialName("sender_id") val senderId: String? = null,
    )

    private companion object {
        const val NEW_MESSAGE_EVENT = "new_message"
        const val ROOM_UPDATED_EVENT = "room_updated"
    }

    private fun JsonObject.toAnyValueMap(): Map<String, Any?> =
        mapValues { (_, element) -> element.toKotlinValue() }

    private fun JsonElement.toKotlinValue(): Any? =
        when (this) {
            JsonNull -> null
            is JsonPrimitive -> booleanOrNull ?: longOrNull ?: doubleOrNull ?: intOrNull ?: contentOrNull
            is JsonArray -> this.map { it.toKotlinValue() }
            is JsonObject -> this.toAnyValueMap()
        }
}
