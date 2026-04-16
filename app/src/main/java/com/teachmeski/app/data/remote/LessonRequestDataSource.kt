package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.ChatRoomPreviewDto
import com.teachmeski.app.data.model.InstructorProfilePreviewDto
import com.teachmeski.app.data.model.LessonRequestDto
import com.teachmeski.app.data.model.LessonRequestListItemDto
import com.teachmeski.app.data.model.SkiResortDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRequestDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun submitLessonRequest(
        userId: String,
        discipline: String,
        skillLevel: Int,
        groupSize: Int,
        hasChildren: Boolean,
        dateStart: String?,
        dateEnd: String?,
        datesFlexible: Boolean,
        durationDays: Double,
        equipmentRental: String,
        needsTransport: Boolean,
        transportNote: String,
        languages: List<String>,
        additionalNotes: String,
        allRegionsSelected: Boolean,
        resortIds: List<String>,
        certPreferences: List<String>,
    ): String {
        val payload = buildJsonObject {
            put("user_id", userId)
            put("discipline", discipline)
            put("skill_level", skillLevel)
            put("group_size", groupSize)
            put("has_children", hasChildren)
            if (dateStart != null) put("date_start", dateStart)
            if (dateEnd != null) put("date_end", dateEnd)
            else if (dateStart != null) put("date_end", dateStart)
            put("dates_flexible", datesFlexible)
            put("duration_days", durationDays)
            put("equipment_rental", equipmentRental)
            put("needs_transport", needsTransport)
            put("transport_note", transportNote)
            put("additional_notes", additionalNotes)
            put("all_regions_selected", allRegionsSelected)
            putJsonArray("resort_ids") { resortIds.forEach { add(JsonPrimitive(it)) } }
            putJsonArray("languages") { languages.forEach { add(JsonPrimitive(it)) } }
            put("status", "active")
        }

        val result = supabaseClient.postgrest
            .from("lesson_requests")
            .insert(payload) {
                select(Columns.list("id"))
            }
            .decodeSingle<LessonRequestDto>()

        if (certPreferences.isNotEmpty()) {
            val certPayloads = certPreferences.map { cert ->
                buildJsonObject {
                    put("lesson_request_id", result.id)
                    put("certification_code", cert)
                }
            }
            supabaseClient.postgrest
                .from("lesson_request_cert_prefs")
                .insert(certPayloads)
        }

        return result.id
    }

    suspend fun getMyLessonRequests(userId: String): List<LessonRequestListItemDto> =
        supabaseClient.postgrest
            .from("lesson_requests")
            .select(
                columns = Columns.raw(
                    "id, discipline, skill_level, group_size, date_start, date_end, dates_flexible, duration_days, status, created_at, chat_rooms(id, instructor_id, last_message_content, last_message_at, last_message_sender_id, last_read_at, instructor_profiles(id, user_id, display_name, avatar_url, short_id, rating_avg, rating_count, phone_verified_at))"
                )
            ) {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<LessonRequestListItemDto>()

    suspend fun getLessonRequestDetail(id: String, userId: String): LessonRequestDto =
        supabaseClient.postgrest
            .from("lesson_requests")
            .select {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<LessonRequestDto>()

    suspend fun getResortsByIds(resortIds: List<String>): List<SkiResortDto> {
        if (resortIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest
            .from("ski_resorts")
            .select {
                filter { isIn("id", resortIds) }
            }
            .decodeList<SkiResortDto>()
    }

    suspend fun getCertPreferences(lessonRequestId: String): List<String> {
        @Serializable
        data class CertPref(
            @SerialName("certification_code") val certificationCode: String,
        )
        return supabaseClient.postgrest
            .from("lesson_request_cert_prefs")
            .select {
                filter { eq("lesson_request_id", lessonRequestId) }
            }
            .decodeList<CertPref>()
            .map { it.certificationCode }
    }

    suspend fun closeLessonRequest(id: String, userId: String) {
        supabaseClient.postgrest
            .from("lesson_requests")
            .update({
                set("status", "closed_by_user")
            }) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
    }

    suspend fun getRecommendedInstructors(
        discipline: String,
        resortIds: List<String>,
        excludeInstructorIds: List<String>,
        limit: Int = 20,
    ): List<InstructorProfilePreviewDto> =
        supabaseClient.postgrest
            .from("instructor_profiles")
            .select(
                columns = Columns.raw(
                    "id, user_id, display_name, avatar_url, short_id, rating_avg, rating_count, phone_verified_at"
                )
            ) {
                filter {
                    eq("is_accepting_requests", true)
                    or {
                        eq("discipline", discipline)
                        eq("discipline", "both")
                    }
                    if (resortIds.isNotEmpty()) {
                        overlaps("resort_ids", resortIds)
                    }
                    if (excludeInstructorIds.isNotEmpty()) {
                        and {
                            excludeInstructorIds.forEach { id ->
                                neq("id", id)
                            }
                        }
                    }
                }
                order("is_test_data", Order.ASCENDING)
                order("rating_avg", Order.DESCENDING)
                order("created_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<InstructorProfilePreviewDto>()

    suspend fun getUnlockedInstructorIds(lessonRequestId: String): List<String> {
        @Serializable
        data class UnlockRow(
            @SerialName("instructor_id") val instructorId: String,
        )
        return supabaseClient.postgrest
            .from("request_unlocks")
            .select(columns = Columns.list("instructor_id")) {
                filter { eq("lesson_request_id", lessonRequestId) }
            }
            .decodeList<UnlockRow>()
            .map { it.instructorId }
    }

    suspend fun getChatRoomsForRequest(
        lessonRequestId: String,
        userId: String,
    ): List<ChatRoomPreviewDto> =
        supabaseClient.postgrest
            .from("chat_rooms")
            .select(
                columns = Columns.raw(
                    "id, instructor_id, last_message_content, last_message_at, last_message_sender_id, last_read_at, instructor_profiles(id, user_id, display_name, avatar_url, short_id, rating_avg, rating_count, phone_verified_at)"
                )
            ) {
                filter {
                    eq("lesson_request_id", lessonRequestId)
                    eq("user_id", userId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ChatRoomPreviewDto>()
}
