package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource

interface ReportRepository {
    suspend fun reportUser(reportedUserId: String, reason: String, roomId: String?): Resource<Unit>
}
