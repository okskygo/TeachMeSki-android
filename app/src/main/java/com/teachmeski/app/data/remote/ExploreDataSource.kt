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
    data class InstructorIdRow(
        val id: String,
    )

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
                    "id, status, created_at, discipline, skill_level, group_size, has_children, duration_days, date_start, date_end, dates_flexible, languages, additional_notes, quota_limit, all_regions_selected, resort_ids, user_id",
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

    suspend fun getUnlockRows(requestIds: List<String>): List<UnlockRow> {
        if (requestIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("request_unlocks")
            .select(columns = Columns.raw("lesson_request_id, instructor_id")) {
                filter { isIn("lesson_request_id", requestIds) }
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
