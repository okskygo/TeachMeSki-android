package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun blockUser(blockerId: String, blockedId: String) {
        supabaseClient.postgrest.from("blocks")
            .insert(
                buildJsonObject {
                    put("blocker_id", blockerId)
                    put("blocked_id", blockedId)
                },
            )
    }

    suspend fun unblockUser(blockerId: String, blockedId: String) {
        supabaseClient.postgrest.from("blocks")
            .delete {
                filter {
                    eq("blocker_id", blockerId)
                    eq("blocked_id", blockedId)
                }
            }
    }
}
