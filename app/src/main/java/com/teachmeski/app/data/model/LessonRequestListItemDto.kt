package com.teachmeski.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonRequestListItemDto(
    val id: String,
    val discipline: String,
    @SerialName("skill_level") val skillLevel: Int,
    @SerialName("group_size") val groupSize: Int,
    @SerialName("date_start") val dateStart: String? = null,
    @SerialName("date_end") val dateEnd: String? = null,
    @SerialName("dates_flexible") val datesFlexible: Boolean = false,
    @SerialName("duration_days") val durationDays: Double = 1.0,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("chat_rooms") val chatRooms: List<ChatRoomPreviewDto> = emptyList(),
)

@Serializable
data class ChatRoomPreviewDto(
    val id: String,
    @SerialName("instructor_id") val instructorId: String,
    @SerialName("last_message_content") val lastMessageContent: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("last_message_sender_id") val lastMessageSenderId: String? = null,
    @SerialName("last_read_at") val lastReadAt: String? = null,
    @SerialName("instructor_profiles") val instructorProfile: InstructorProfilePreviewDto? = null,
)

@Serializable
data class InstructorProfilePreviewDto(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("short_id") val shortId: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double? = null,
    @SerialName("rating_count") val ratingCount: Int = 0,
    @SerialName("line_user_id") val lineUserId: String? = null,
)
