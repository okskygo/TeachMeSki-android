package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource

interface ReviewRepository {
    suspend fun submitReview(instructorId: String, rating: Int, comment: String?): Resource<Unit>
}
