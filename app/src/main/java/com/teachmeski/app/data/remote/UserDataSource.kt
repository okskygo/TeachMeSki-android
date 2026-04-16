package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.UserDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
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
}
