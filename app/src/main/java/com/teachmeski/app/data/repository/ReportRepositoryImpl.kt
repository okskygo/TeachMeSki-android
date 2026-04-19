package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.remote.ReportDataSource
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ReportRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDataSource: ReportDataSource,
    private val authRepository: AuthRepository,
) : ReportRepository {

    override suspend fun reportUser(reportedUserId: String, reason: String, roomId: String?): Resource<Unit> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            reportDataSource.reportUser(userId, reportedUserId, reason, roomId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }
}
