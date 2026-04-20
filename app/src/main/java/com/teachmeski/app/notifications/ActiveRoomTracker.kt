package com.teachmeski.app.notifications

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks the room id the user is currently viewing (if any), so that
 * [TmsFirebaseMessagingService] can suppress in-room message pushes and so that
 * [NotificationDisplay] can clear stale notifications for the active room.
 *
 * Thread-safe; singleton-scoped via Hilt.
 */
@Singleton
class ActiveRoomTracker @Inject constructor() {

    private val activeRoomId = AtomicReference<String?>(null)

    fun setActiveRoom(roomId: String) {
        activeRoomId.set(roomId)
    }

    fun clearActiveRoom(roomId: String) {
        activeRoomId.compareAndSet(roomId, null)
    }

    fun isActive(roomId: String?): Boolean {
        if (roomId.isNullOrBlank()) return false
        return activeRoomId.get() == roomId
    }

    fun current(): String? = activeRoomId.get()
}
