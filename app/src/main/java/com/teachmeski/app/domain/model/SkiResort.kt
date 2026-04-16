package com.teachmeski.app.domain.model

data class SkiResort(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val nameJa: String?,
    val regionId: String,
    val isOther: Boolean,
    val sortOrder: Int,
)
