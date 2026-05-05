package com.teachmeski.app.notifications

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton event bus signalling "the user's unread count may have changed —
 * please re-fetch and update the bottom-tab badge."
 *
 * Emitters:
 *  - `ChatViewModel.markRead()` after a successful `markRoomAsRead` RPC.
 *
 * Subscribers:
 *  - `MainViewModel` — calls `refreshUnreadCount()` so the badge clears
 *    promptly when the user reads a conversation, even though Supabase does
 *    not broadcast a realtime event for `last_read_at` updates.
 *
 * iOS solves the same problem by passing an `onUnreadChanged` closure into
 * `ChatViewModel` via `AppContainer` DI. On Android, callback-injection
 * through Hilt is awkward, so a singleton bus is the idiomatic alternative.
 */
@Singleton
class UnreadCountInvalidator @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun invalidate() {
        _events.tryEmit(Unit)
    }
}
