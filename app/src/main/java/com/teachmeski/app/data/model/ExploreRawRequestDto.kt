package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExploreRawRequestDto(
    val id: String,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String = "",
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
    @SerialName("equipment_rental") val equipmentRental: String? = null,
    @SerialName("needs_transport") val needsTransport: Boolean = false,
    @SerialName("transport_note") val transportNote: String? = null,
    @SerialName("quota_limit") val quotaLimit: Int = 5,
    @SerialName("all_regions_selected") val allRegionsSelected: Boolean = false,
    @SerialName("resort_ids") val resortIds: List<String> = emptyList(),
    @SerialName("user_id") val userId: String = "",
)

fun ExploreRawRequestDto.toExploreLessonRequest(
    unlockCount: Int,
    isUnlockedByMe: Boolean,
    myChatRoomId: String?,
    userDisplayName: String,
    userAvatarUrl: String?,
    resortNames: List<String>,
    baseTokenCost: Int,
    certPreferences: List<String>,
): ExploreLessonRequest {
    return ExploreLessonRequest(
        id = id,
        status = LessonRequestStatus.fromString(status),
        createdAt = createdAt,
        discipline = Discipline.fromString(discipline),
        skillLevel = skillLevel,
        groupSize = groupSize,
        hasChildren = hasChildren,
        durationDays = durationDays,
        startDate = dateStart,
        endDate = dateEnd,
        datesFlexible = datesFlexible,
        preferredLanguages = languages,
        additionalNotes = additionalNotes,
        equipmentRental = EquipmentRental.fromNullableString(equipmentRental),
        needsTransport = needsTransport,
        transportNote = transportNote?.takeIf { it.isNotBlank() },
        certPreferences = certPreferences,
        quotaLimit = quotaLimit,
        unlockCount = unlockCount,
        baseTokenCost = baseTokenCost,
        userDisplayName = userDisplayName,
        userAvatarUrl = userAvatarUrl,
        allRegionsSelected = allRegionsSelected,
        resortNames = resortNames,
        isUnlockedByMe = isUnlockedByMe,
        myChatRoomId = myChatRoomId,
    )
}
