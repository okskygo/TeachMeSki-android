package com.teachmeski.app.data.repository

import android.util.Log
import com.teachmeski.app.R
import com.teachmeski.app.data.remote.DeviceTokenDataSource
import com.teachmeski.app.domain.repository.DeviceTokenRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTokenRepositoryImpl @Inject constructor(
    private val deviceTokenDataSource: DeviceTokenDataSource,
) : DeviceTokenRepository {

    override suspend fun upsertAndroidToken(
        userId: String,
        token: String,
        locale: String,
    ): Resource<Unit> {
        return try {
            deviceTokenDataSource.upsertAndroidToken(userId, token, locale)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "upsertAndroidToken failed", e)
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }

    override suspend fun deleteToken(token: String): Resource<Unit> {
        return try {
            deviceTokenDataSource.deleteToken(token)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "deleteToken failed", e)
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }

    companion object {
        private const val TAG = "DeviceTokenRepo"
    }
}
