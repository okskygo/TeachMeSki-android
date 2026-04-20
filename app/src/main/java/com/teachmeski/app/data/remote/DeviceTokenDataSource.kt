package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class DeviceTokenUpsert(
    @SerialName("user_id") val userId: String,
    val token: String,
    val platform: String,
    val locale: String,
)

@Singleton
class DeviceTokenDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun upsertAndroidToken(userId: String, token: String, locale: String) {
        supabaseClient.postgrest["device_tokens"]
            .upsert(
                DeviceTokenUpsert(
                    userId = userId,
                    token = token,
                    platform = PLATFORM_ANDROID,
                    locale = locale,
                ),
            ) {
                onConflict = "token"
            }
    }

    suspend fun deleteToken(token: String) {
        supabaseClient.postgrest["device_tokens"]
            .delete {
                filter { eq("token", token) }
            }
    }

    companion object {
        const val PLATFORM_ANDROID = "android"
    }
}
