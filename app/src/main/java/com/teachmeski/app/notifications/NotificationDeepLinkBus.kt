package com.teachmeski.app.notifications

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot queue for pending notification deep links.
 *
 * MainActivity.onNewIntent / onCreate pushes an incoming intent here.
 * TeachMeSkiRoot collects it and routes the NavController once
 * MainUiState is Authenticated (and switches role if needed).
 */
data class NotificationDeepLinkEvent(
    val event: String,
    val roomId: String? = null,
    val requestId: String? = null,
    val transactionId: String? = null,
)

@Singleton
class NotificationDeepLinkBus @Inject constructor() {
    private val channel = Channel<NotificationDeepLinkEvent>(capacity = Channel.BUFFERED)
    val events: Flow<NotificationDeepLinkEvent> = channel.receiveAsFlow()

    fun emit(event: NotificationDeepLinkEvent) {
        channel.trySend(event)
    }

    /**
     * F-113 FR-113-019 / AC-113-014: drain any pending events from the
     * channel so that a push that arrived while the user was signed out
     * is NOT replayed after a fresh sign-in. Called by `MainViewModel`
     * on the Authenticated → Unauthenticated transition.
     */
    fun clearPending() {
        while (channel.tryReceive().isSuccess) {
            // drop
        }
    }
}
