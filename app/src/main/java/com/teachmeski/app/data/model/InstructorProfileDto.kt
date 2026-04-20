package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.InstructorProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorProfileDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("short_id") val shortId: String = "",
    @SerialName("display_name") val displayName: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val discipline: String = "ski",
    @SerialName("teachable_levels") val teachableLevels: List<Int> = emptyList(),
    val certifications: List<String> = emptyList(),
    @SerialName("certification_other") val certificationOther: String? = null,
    val languages: List<String> = emptyList(),
    @SerialName("price_half_day") val priceHalfDay: Int? = null,
    @SerialName("price_full_day") val priceFullDay: Int? = null,
    @SerialName("offers_transport") val offersTransport: Boolean = false,
    @SerialName("offers_photography") val offersPhotography: Boolean = false,
    @SerialName("is_accepting_requests") val isAcceptingRequests: Boolean = true,
    @SerialName("certificate_urls") val certificateUrls: List<String> = emptyList(),
    @SerialName("rating_avg") val ratingAvg: Double? = null,
    @SerialName("rating_count") val ratingCount: Int = 0,
    @SerialName("view_count") val viewCount: Int = 0,
    val phone: String? = null,
    @SerialName("phone_verified_at") val phoneVerifiedAt: String? = null,
    @SerialName("resort_ids") val resortIds: List<String> = emptyList(),
)

fun InstructorProfileDto.toDomain(
    email: String = "",
    resortNames: List<String> = emptyList(),
) = InstructorProfile(
    id = id,
    userId = userId,
    shortId = shortId,
    displayName = displayName ?: "",
    bio = bio,
    avatarUrl = avatarUrl,
    email = email,
    discipline = Discipline.fromString(discipline),
    teachableLevels = teachableLevels,
    certifications = certifications,
    certificationOther = certificationOther,
    languages = languages,
    priceHalfDay = priceHalfDay,
    priceFullDay = priceFullDay,
    offersTransport = offersTransport,
    offersPhotography = offersPhotography,
    isAcceptingRequests = isAcceptingRequests,
    certificateUrls = certificateUrls,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
    viewCount = viewCount,
    phone = phone,
    phoneVerifiedAt = phoneVerifiedAt,
    resortIds = resortIds,
    resortNames = resortNames,
)
