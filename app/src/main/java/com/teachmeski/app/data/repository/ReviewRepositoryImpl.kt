package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.remote.ReviewDataSource
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ReviewRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewDataSource: ReviewDataSource,
    private val authRepository: AuthRepository,
) : ReviewRepository {

    override suspend fun submitReview(instructorId: String, rating: Int, comment: String?): Resource<Unit> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            reviewDataSource.submitReview(userId, instructorId, rating, comment)
            Resource.Success(Unit)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("duplicate") || msg.contains("unique")) {
                Resource.Error(UiText.StringResource(R.string.review_error_duplicate))
            } else {
                Resource.Error(UiText.StringResource(R.string.error_generic))
            }
        }
    }
}
