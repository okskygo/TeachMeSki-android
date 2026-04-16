package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.SkiResort
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkiResortDto(
    val id: String,
    @SerialName("name_zh") val nameZh: String,
    @SerialName("name_en") val nameEn: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("region_id") val regionId: String,
    @SerialName("is_other") val isOther: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

fun SkiResortDto.toDomain() =
    SkiResort(
        id = id,
        nameZh = nameZh,
        nameEn = nameEn,
        nameJa = nameJa,
        regionId = regionId,
        isOther = isOther,
        sortOrder = sortOrder,
    )
