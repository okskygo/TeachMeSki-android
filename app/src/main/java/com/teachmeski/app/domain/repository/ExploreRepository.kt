package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.util.Resource

interface ExploreRepository {
    suspend fun getExploreLessonRequests(
        page: Int,
        disciplineFilter: List<String>?,
        resortFilter: List<String>?,
    ): Resource<Pair<List<ExploreLessonRequest>, Int>>

    suspend fun unlockLessonRequest(
        lessonRequestId: String,
        message: String,
    ): Resource<String>
}
