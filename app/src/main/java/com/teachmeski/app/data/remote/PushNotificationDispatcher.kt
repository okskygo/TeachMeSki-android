package com.teachmeski.app.data.remote

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fires server-side push-notification dispatches via the
 * `send-push-notification` Edge Function. Each event family is exposed as a
 * dedicated method so call-sites cannot accidentally pass a wrong payload
 * shape.
 *
 * All methods are best-effort: they catch and log any exception so callers
 * can keep the success path of the originating action regardless of push
 * delivery (see `activate-pending-requests/index.ts:48-69` for the canonical
 * N-001 example).
 */
@Singleton
class PushNotificationDispatcher @Inject constructor(
    private val supabase: SupabaseClient,
) {
    /**
     * F-109-N007 (FR-N007-005): notify every `is_accepting_requests=true`
     * instructor that a lesson request just had its quota expanded.
     *
     * Mirrors the iOS `PushNotificationDispatcher.fireN007QuotaExpanded`
     * (Task 4 of the F-109 N-007 plan). The Edge Function ignores anything
     * outside `event` / `reference_id`, so we keep the body minimal.
     */
    suspend fun fireN007QuotaExpanded(lessonRequestId: String) {
        try {
            supabase.functions.invoke(
                function = "send-push-notification",
                body = N007Body(event = "N-007", referenceId = lessonRequestId),
            )
        } catch (e: Exception) {
            Log.e(TAG, "fireN007QuotaExpanded failed for $lessonRequestId", e)
        }
    }

    @Serializable
    private data class N007Body(
        val event: String,
        @SerialName("reference_id") val referenceId: String,
    )

    private companion object {
        const val TAG = "PushDispatcher"
    }
}
