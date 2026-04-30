package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the device's FCM token in `device_tokens` via SECURITY DEFINER RPCs
 * (F-111). The RPCs let us atomically take over a token previously bound to
 * another user and release it cleanly on sign-out, neither of which is
 * possible against the table directly because RLS (`auth.uid() = user_id`)
 * blocks updates / deletes targeting another user's row.
 */
@Singleton
class DeviceTokenDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun upsertAndroidToken(userId: String, token: String, locale: String) {
        val params = buildJsonObject {
            put("p_user_id", userId)
            put("p_token", token)
            put("p_platform", PLATFORM_ANDROID)
            put("p_locale", locale)
        }
        supabaseClient.postgrest.rpc("claim_device_token", params)
    }

    suspend fun deleteToken(token: String) {
        val params = buildJsonObject {
            put("p_token", token)
        }
        supabaseClient.postgrest.rpc("release_device_token", params)
    }

    companion object {
        const val PLATFORM_ANDROID = "android"
    }
}
