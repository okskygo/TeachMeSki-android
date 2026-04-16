package com.teachmeski.app.domain.model

data class LessonRequestListItem(
    val id: String,
    val discipline: Discipline,
    val skillLevel: Int,
    val groupSize: Int,
    val dateStart: String?,
    val dateEnd: String?,
    val datesFlexible: Boolean,
    val durationDays: Double,
    val status: LessonRequestStatus,
    val createdAt: String,
    val chatCount: Int,
    val instructorPreviews: List<InstructorPreview>,
)
