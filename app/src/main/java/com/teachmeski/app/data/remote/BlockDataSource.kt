package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
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

    /**
     * Returns true iff the signed-in user has a block row for [blockedId].
     * The SELECT RLS on `blocks` allows the blocker to read their own rows.
     */
    suspend fun haveIBlocked(blockerId: String, blockedId: String): Boolean {
        val result = supabaseClient.postgrest.from("blocks")
            .select {
                filter {
                    eq("blocker_id", blockerId)
                    eq("blocked_id", blockedId)
                }
                limit(1)
            }
        return result.decodeList<kotlinx.serialization.json.JsonObject>().isNotEmpty()
    }

    /**
     * F-110: SECURITY DEFINER RPC `am_i_blocked_by(other_user_id uuid)
     * returns boolean`. Wraps the call so callers don't have to know the
     * RPC contract.
     */
    suspend fun amIBlockedBy(otherUserId: String): Boolean {
        val params = buildJsonObject {
            put("other_user_id", otherUserId)
        }
        val response = supabaseClient.postgrest.rpc("am_i_blocked_by", params)
        val element = response.decodeAs<JsonPrimitive>()
        return element.booleanOrNull
            ?: element.content.equals("true", ignoreCase = true)
    }
}
