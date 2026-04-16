package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.UserDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun getUserById(userId: String): UserDto =
        supabaseClient.postgrest
            .from("users")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingle<UserDto>()

    suspend fun updateDisplayName(userId: String, displayName: String) {
        supabaseClient.postgrest
            .from("users")
            .update({
                set("display_name", displayName)
            }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, extension: String): String {
        val bucket = supabaseClient.storage.from("avatars")
        val path = "$userId/${System.currentTimeMillis()}.$extension"

        val currentUser = getUserById(userId)
        val oldUrl = currentUser.avatarUrl
        if (!oldUrl.isNullOrBlank()) {
            val marker = "/storage/v1/object/public/avatars/"
            val idx = oldUrl.indexOf(marker)
            if (idx >= 0) {
                val oldPath = oldUrl.substring(idx + marker.length)
                try { bucket.delete(oldPath) } catch (_: Exception) { }
            }
        }

        bucket.upload(path, imageBytes) { upsert = true }

        val publicUrl = bucket.publicUrl(path)

        supabaseClient.postgrest
            .from("users")
            .update({
                set("avatar_url", publicUrl)
            }) {
                filter { eq("id", userId) }
            }

        return publicUrl
    }
}
