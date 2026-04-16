package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.LessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonRequestDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val discipline: String = "ski",
    @SerialName("skill_level") val skillLevel: Int = 0,
    @SerialName("group_size") val groupSize: Int = 1,
    @SerialName("has_children") val hasChildren: Boolean = false,
    @SerialName("date_start") val dateStart: String? = null,
    @SerialName("date_end") val dateEnd: String? = null,
    @SerialName("dates_flexible") val datesFlexible: Boolean = false,
    @SerialName("duration_days") val durationDays: Double = 1.0,
    @SerialName("equipment_rental") val equipmentRental: String = "none",
    @SerialName("needs_transport") val needsTransport: Boolean = false,
    @SerialName("transport_note") val transportNote: String? = null,
    val languages: List<String> = listOf("zh"),
    @SerialName("additional_notes") val additionalNotes: String? = null,
    @SerialName("all_regions_selected") val allRegionsSelected: Boolean = false,
    @SerialName("resort_ids") val resortIds: List<String> = emptyList(),
    val status: String = "active",
    @SerialName("quota_limit") val quotaLimit: Int = 5,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("expires_at") val expiresAt: String? = null,
)

fun LessonRequestDto.toDomain(
    resortNames: List<String> = emptyList(),
    certPreferences: List<String> = emptyList(),
) =
    LessonRequest(
        id = id,
        userId = userId,
        discipline = Discipline.fromString(discipline),
        skillLevel = skillLevel,
        groupSize = groupSize,
        hasChildren = hasChildren,
        dateStart = dateStart,
        dateEnd = dateEnd,
        datesFlexible = datesFlexible,
        durationDays = durationDays,
        equipmentRental = EquipmentRental.fromString(equipmentRental),
        needsTransport = needsTransport,
        transportNote = transportNote,
        languages = languages,
        additionalNotes = additionalNotes,
        allRegionsSelected = allRegionsSelected,
        resortIds = resortIds,
        status = LessonRequestStatus.fromString(status),
        quotaLimit = quotaLimit,
        createdAt = createdAt,
        expiresAt = expiresAt,
        resortNames = resortNames,
        certPreferences = certPreferences,
    )
