package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.Region
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegionDto(
    val id: String,
    @SerialName("name_zh") val nameZh: String,
    @SerialName("name_en") val nameEn: String,
    @SerialName("prefecture_zh") val prefectureZh: String? = null,
    @SerialName("prefecture_en") val prefectureEn: String? = null,
    val country: String = "JP",
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("ski_resorts") val skiResorts: List<SkiResortDto> = emptyList(),
)

fun RegionDto.toDomain() =
    Region(
        id = id,
        nameZh = nameZh,
        nameEn = nameEn,
        prefectureZh = prefectureZh,
        prefectureEn = prefectureEn,
        country = country,
        sortOrder = sortOrder,
        resorts = skiResorts.map { it.toDomain() },
    )
