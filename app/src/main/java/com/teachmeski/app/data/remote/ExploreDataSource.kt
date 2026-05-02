package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.ExploreRawRequestDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
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
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 20

@Singleton
class ExploreDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    @Serializable
    data class UnlockRow(
        @SerialName("lesson_request_id") val lessonRequestId: String,
        @SerialName("instructor_id") val instructorId: String,
    )

    @Serializable
    data class ChatRoomRow(
        val id: String,
        @SerialName("lesson_request_id") val lessonRequestId: String,
    )

    @Serializable
    data class UserRow(
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
    data class CertPrefRow(
        @SerialName("lesson_request_id") val lessonRequestId: String,
        @SerialName("certification_code") val certificationCode: String,
    )

    @Serializable
    data class InstructorIdRow(
        val id: String,
    )

    /**
     * F-008 P3: latest `request_unlocks` row per (lesson_request_id, instructor_id) pair,
     * used by `UnlockedScreen` to grey out refunded cards. A given (instructor,
     * lesson_request) pair may have multiple historical rows because Path-A
     * instructors can re-unlock a refunded room.
     */
    @Serializable
    data class UnlockedRequestUnlockRow(
        @SerialName("lesson_request_id") val lessonRequestId: String,
        val status: String,
        @SerialName("unlocked_at") val unlockedAt: String,
    )

    @Serializable
    data class UnlockedRoomDto(
        val id: String,
        @SerialName("lesson_request_id") val lessonRequestId: String,
        @SerialName("instructor_id") val instructorId: String,
        @SerialName("user_id") val userId: String,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("last_message_content") val lastMessageContent: String? = null,
        @SerialName("last_message_at") val lastMessageAt: String? = null,
        @SerialName("last_message_sender_id") val lastMessageSenderId: String? = null,
        @SerialName("instructor_last_read_at") val instructorLastReadAt: String? = null,
    )

    @Serializable
    data class UnlockedRequestSummaryDto(
        val id: String,
        val discipline: String,
        val status: String = "active",
        val languages: List<String> = emptyList(),
        @SerialName("skill_level") val skillLevel: Int? = null,
        @SerialName("group_size") val groupSize: Int = 1,
        @SerialName("has_children") val hasChildren: Boolean = false,
        @SerialName("duration_days") val durationDays: Double? = null,
        @SerialName("additional_notes") val additionalNotes: String? = null,
        @SerialName("date_start") val dateStart: String? = null,
        @SerialName("date_end") val dateEnd: String? = null,
        @SerialName("dates_flexible") val datesFlexible: Boolean = false,
        @SerialName("all_regions_selected") val allRegionsSelected: Boolean = false,
        @SerialName("resort_ids") val resortIds: List<String> = emptyList(),
    )

    suspend fun getUnlockedRooms(instructorProfileId: String): List<UnlockedRoomDto> =
        supabaseClient.postgrest.from("chat_rooms")
            .select(
                columns = Columns.raw(
                    "id, lesson_request_id, instructor_id, user_id, created_at, last_message_content, last_message_at, last_message_sender_id, instructor_last_read_at",
                ),
            ) {
                filter { eq("instructor_id", instructorProfileId) }
                order("last_message_at", Order.DESCENDING)
            }
            .decodeList<UnlockedRoomDto>()

    suspend fun getLessonRequestSummaries(requestIds: List<String>): List<UnlockedRequestSummaryDto> {
        if (requestIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("lesson_requests")
            .select(
                columns = Columns.raw(
                    "id, status, discipline, languages, skill_level, group_size, has_children, duration_days, additional_notes, date_start, date_end, dates_flexible, all_regions_selected, resort_ids",
                ),
            ) {
                filter { isIn("id", requestIds) }
            }
            .decodeList<UnlockedRequestSummaryDto>()
    }

    suspend fun getExploreLessonRequests(
        currentUserId: String,
        page: Int,
        disciplineFilter: List<String>?,
        resortFilter: List<String>?,
    ): Pair<List<ExploreRawRequestDto>, Int> {
        val safePage = maxOf(1, page)
        val from = (safePage - 1) * PAGE_SIZE
        val to = from + PAGE_SIZE - 1

        val query = supabaseClient.postgrest.from("lesson_requests")
            .select(
                columns = Columns.raw(
                    "id, status, created_at, discipline, skill_level, group_size, has_children, duration_days, date_start, date_end, dates_flexible, languages, additional_notes, equipment_rental, needs_transport, transport_note, quota_limit, unlock_count, all_regions_selected, resort_ids, user_id",
                ),
                request = {
                    filter {
                        isIn("status", listOf("active", "locked"))
                        neq("user_id", currentUserId)
                        if (!disciplineFilter.isNullOrEmpty() && disciplineFilter.size == 1) {
                            or {
                                eq("discipline", disciplineFilter[0])
                                eq("discipline", "both")
                            }
                        }
                        if (!resortFilter.isNullOrEmpty()) {
                            or {
                                overlaps("resort_ids", resortFilter)
                                eq("all_regions_selected", true)
                            }
                        }
                    }
                    order("created_at", Order.DESCENDING)
                    range(from.toLong(), to.toLong())
                    count(Count.EXACT)
                },
            )

        val result = query.decodeList<ExploreRawRequestDto>()
        val totalCount = query.countOrNull()?.toInt() ?: result.size

        return Pair(result, totalCount)
    }

    /**
     * Returns the calling instructor's own unlock rows for the given requests.
     *
     * Used only to compute "is_unlocked_by_me" on the explore feed. The global
     * unlock_count comes from lesson_requests.unlock_count (kept in sync by a
     * DB trigger), because RLS on request_unlocks hides other instructors'
     * rows so client-side COUNT(*) is wrong by design.
     */
    /**
     * F-008 P3: returns this instructor's `request_unlocks` rows for the given
     * lesson requests, ordered by `unlocked_at DESC` so callers can take the
     * first row per `lesson_request_id` to know whether the most recent unlock
     * is still `'active'`, has been auto-`'refunded'`, or has been
     * auto-`'completed'`. RLS still scopes rows to this instructor.
     */
    suspend fun getMyRequestUnlockRows(
        instructorProfileId: String,
        requestIds: List<String>,
    ): List<UnlockedRequestUnlockRow> {
        if (requestIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("request_unlocks")
            .select(columns = Columns.raw("lesson_request_id, status, unlocked_at")) {
                filter {
                    eq("instructor_id", instructorProfileId)
                    isIn("lesson_request_id", requestIds)
                }
                order("unlocked_at", Order.DESCENDING)
            }
            .decodeList<UnlockedRequestUnlockRow>()
    }

    suspend fun getMyUnlockRows(instructorProfileId: String, requestIds: List<String>): List<UnlockRow> {
        if (requestIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("request_unlocks")
            .select(columns = Columns.raw("lesson_request_id, instructor_id")) {
                filter {
                    eq("instructor_id", instructorProfileId)
                    isIn("lesson_request_id", requestIds)
                }
            }
            .decodeList<UnlockRow>()
    }

    suspend fun getChatRoomsForInstructor(instructorProfileId: String, requestIds: List<String>): List<ChatRoomRow> {
        if (requestIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("chat_rooms")
            .select(columns = Columns.raw("id, lesson_request_id")) {
                filter {
                    eq("instructor_id", instructorProfileId)
                    isIn("lesson_request_id", requestIds)
                }
            }
            .decodeList<ChatRoomRow>()
    }

    suspend fun getUserRows(userIds: List<String>): List<UserRow> {
        if (userIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("users")
            .select(columns = Columns.raw("id, display_name, avatar_url")) {
                filter { isIn("id", userIds) }
            }
            .decodeList<UserRow>()
    }

    suspend fun getCertPrefs(requestIds: List<String>): List<CertPrefRow> {
        if (requestIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("lesson_request_cert_prefs")
            .select(columns = Columns.raw("lesson_request_id, certification_code")) {
                filter { isIn("lesson_request_id", requestIds) }
            }
            .decodeList<CertPrefRow>()
    }

    suspend fun getResortNames(resortIds: List<String>): List<ResortNameRow> {
        if (resortIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("ski_resorts")
            .select(columns = Columns.raw("id, name_zh, name_en")) {
                filter { isIn("id", resortIds) }
            }
            .decodeList<ResortNameRow>()
    }

    suspend fun getInstructorProfileId(userId: String): String? {
        val result = supabaseClient.postgrest.from("instructor_profiles")
            .select(columns = Columns.list("id")) {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<InstructorIdRow>()
        return result?.id
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

    private fun JsonObject.toAnyValueMap(): Map<String, Any?> =
        mapValues { (_, element) -> element.toKotlinValue() }

    private fun JsonElement.toKotlinValue(): Any? =
        when (this) {
            JsonNull -> null
            is JsonPrimitive ->
                booleanOrNull
                    ?: longOrNull
                    ?: doubleOrNull
                    ?: intOrNull
                    ?: contentOrNull
            is JsonArray -> this.map { it.toKotlinValue() }
            is JsonObject -> this.toAnyValueMap()
        }
}
