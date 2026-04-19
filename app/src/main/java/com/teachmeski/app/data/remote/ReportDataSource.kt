package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun reportUser(reporterId: String, reportedId: String, reason: String, roomId: String?) {
        supabaseClient.postgrest.from("reports")
            .insert(
                buildJsonObject {
                    put("reporter_id", reporterId)
                    put("reported_id", reportedId)
                    put("reason", reason)
                    if (roomId != null) put("room_id", roomId)
                },
            )
    }
}
