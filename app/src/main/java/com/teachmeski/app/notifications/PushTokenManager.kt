package com.teachmeski.app.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.DeviceTokenRepository
import com.teachmeski.app.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers / unregisters the current device's FCM token in the `device_tokens` table.
 *
 * - Called on every successful auth session (MainViewModel.resolveRole → registerCurrentDeviceToken)
 * - Called before every sign-out (AccountSettings / InstructorAccountSettings)
 * - Called on onNewToken() from FCM when the user is already authenticated
 */
@Singleton
class PushTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebaseMessaging: FirebaseMessaging,
) {

    suspend fun registerCurrentDeviceToken(userId: String) {
        try {
            val token = firebaseMessaging.token.await()
            if (token.isNullOrBlank()) {
                Log.w(TAG, "FCM returned blank token")
                return
            }
            upsert(userId, token)
        } catch (e: Exception) {
            Log.w(TAG, "registerCurrentDeviceToken failed", e)
        }
    }

    suspend fun onTokenRefreshed(token: String) {
        val userId = authRepository.currentUserId() ?: return
        upsert(userId, token)
    }

    suspend fun unregisterCurrentDeviceToken() {
        try {
            val token = firebaseMessaging.token.await()
            if (token.isNullOrBlank()) return
            when (val res = deviceTokenRepository.deleteToken(token)) {
                is Resource.Error -> Log.w(TAG, "deleteToken error: ${res.message}")
                else -> Unit
            }
        } catch (e: Exception) {
            Log.w(TAG, "unregisterCurrentDeviceToken failed", e)
        }
    }

    private suspend fun upsert(userId: String, token: String) {
        val locale = resolveLocale()
        when (val res = deviceTokenRepository.upsertAndroidToken(userId, token, locale)) {
            is Resource.Error -> Log.w(TAG, "upsert error: ${res.message}")
            else -> Unit
        }
    }

    private fun resolveLocale(): String {
        val tag = Locale.getDefault().toLanguageTag()
        return if (tag.startsWith("zh", ignoreCase = true)) "zh-TW" else "en"
    }

    companion object {
        private const val TAG = "PushTokenManager"
    }
}
