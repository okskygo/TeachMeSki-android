package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource

interface DeviceTokenRepository {
    suspend fun upsertAndroidToken(userId: String, token: String, locale: String): Resource<Unit>
    suspend fun deleteToken(token: String): Resource<Unit>
}
