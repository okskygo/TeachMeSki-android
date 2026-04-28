package com.teachmeski.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonRequestDetailDto(
    val id: String,
    val discipline: String = "ski",
    @SerialName("skill_level") val skillLevel: Int? = null,
    @SerialName("group_size") val groupSize: Int = 1,
    @SerialName("has_children") val hasChildren: Boolean = false,
    @SerialName("duration_days") val durationDays: Double? = null,
    @SerialName("date_start") val dateStart: String? = null,
    @SerialName("date_end") val dateEnd: String? = null,
    @SerialName("dates_flexible") val datesFlexible: Boolean = false,
    val languages: List<String> = emptyList(),
    @SerialName("additional_notes") val additionalNotes: String? = null,
    @SerialName("all_regions_selected") val allRegionsSelected: Boolean = false,
    @SerialName("resort_ids") val resortIds: List<String> = emptyList(),
    @SerialName("equipment_rental") val equipmentRental: String? = null,
    @SerialName("needs_transport") val needsTransport: Boolean = false,
    @SerialName("transport_note") val transportNote: String? = null,
)

@Serializable
data class CertPrefRow(
    @SerialName("certification_code") val certificationCode: String,
)

@Serializable
data class InstructorProfileDetailDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("short_id") val shortId: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double? = null,
    @SerialName("rating_count") val ratingCount: Int = 0,
    val bio: String? = null,
    @SerialName("line_user_id") val lineUserId: String? = null,
)

@Serializable
data class ReviewCheckRow(
    @SerialName("instructor_id") val instructorId: String,
)

@Serializable
data class BlockCheckRow(
    @SerialName("blocker_id") val blockerId: String,
    @SerialName("blocked_id") val blockedId: String,
)

@Serializable
data class UnlockCheckRow(
    @SerialName("instructor_id") val instructorId: String,
    @SerialName("lesson_request_id") val lessonRequestId: String,
)
