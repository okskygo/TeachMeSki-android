package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.LessonRequest
import com.teachmeski.app.domain.model.LessonRequestListItem
import com.teachmeski.app.util.Resource

interface LessonRequestRepository {
    suspend fun submitLessonRequest(
        discipline: String,
        skillLevel: Int,
        groupSize: Int,
        hasChildren: Boolean,
        dateStart: String?,
        dateEnd: String?,
        datesFlexible: Boolean,
        durationDays: Double,
        equipmentRental: String,
        needsTransport: Boolean,
        transportNote: String,
        languages: List<String>,
        additionalNotes: String,
        allRegionsSelected: Boolean,
        resortIds: List<String>,
        certPreferences: List<String>,
    ): Resource<String>

    suspend fun getMyLessonRequests(): Resource<List<LessonRequestListItem>>
    suspend fun getLessonRequestDetail(id: String): Resource<LessonRequest>
    suspend fun closeLessonRequest(id: String): Resource<Unit>
    suspend fun getUnlockedInstructors(lessonRequestId: String): Resource<List<InstructorPreview>>
    suspend fun getRecommendedInstructors(lessonRequestId: String): Resource<List<InstructorPreview>>
}
