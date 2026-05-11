package com.teachmeski.app.data.repository

import android.util.Log
import com.teachmeski.app.domain.repository.UserActivityRepository
import javax.inject.Inject

/** Minimal seam for tests; production binding adapts the supabase-kt client. */
interface SupabaseRpcCalling {
    suspend fun rpc(name: String)
}

class UserActivityRepositoryImpl @Inject constructor(
    private val rpcCaller: SupabaseRpcCalling,
) : UserActivityRepository {
    override suspend fun touch() {
        try {
            rpcCaller.rpc("touch_last_active")
        } catch (t: Throwable) {
            Log.w("UserActivity", "touch_last_active failed", t)
        }
    }
}
