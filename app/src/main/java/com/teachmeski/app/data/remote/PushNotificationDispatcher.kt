package com.teachmeski.app.data.remote

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fires server-side push-notification dispatches via the
 * `send-push-notification` Edge Function. Each event family is exposed as a
 * dedicated method so call-sites cannot accidentally pass a wrong payload
 * shape.
 *
 * All methods are TRUE fire-and-forget: they `launch` on an internal
 * application-scoped supervisor and return immediately. Transport errors
 * are caught and logged inside the launched coroutine. Callers MUST NOT
 * be coupled to push delivery latency — the originating action's success
 * path must complete in O(RPC) time, not O(fan-out × FCM round-trip).
 *
 * F-109 §AC-N007-007: a single quota expansion fan-out can take seconds
 * because the Edge Function loops every `is_accepting_requests=true`
 * instructor's device tokens. Awaiting that here previously made the
 * student's "find more instructors" tap feel hung.
 */
@Singleton
class PushNotificationDispatcher @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * F-109-N007 (FR-N007-005, FR-N007-006): notify every
     * `is_accepting_requests=true` instructor that a lesson request just had
     * its quota expanded. Returns immediately; the actual Edge Function
     * invocation runs on the application-scoped IO dispatcher.
     */
    fun fireN007QuotaExpanded(lessonRequestId: String) {
        scope.launch {
            try {
                supabase.functions.invoke(
                    function = "send-push-notification",
                    body = N007Body(event = "N-007", referenceId = lessonRequestId),
                )
            } catch (e: Exception) {
                Log.e(TAG, "fireN007QuotaExpanded failed for $lessonRequestId", e)
            }
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
