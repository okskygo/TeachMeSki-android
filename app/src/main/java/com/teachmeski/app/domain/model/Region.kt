package com.teachmeski.app.domain.model

data class Region(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val prefectureZh: String?,
    val prefectureEn: String?,
    val country: String,
    val sortOrder: Int,
    val resorts: List<SkiResort>,
)
