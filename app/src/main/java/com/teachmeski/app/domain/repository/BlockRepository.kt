package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource

interface BlockRepository {
    suspend fun blockUser(blockedUserId: String): Resource<Unit>
    suspend fun unblockUser(blockedUserId: String): Resource<Unit>
}
