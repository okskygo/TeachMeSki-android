package com.teachmeski.app.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TmsFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushTokenManager: PushTokenManager

    @Inject
    lateinit var activeRoomTracker: ActiveRoomTracker

    private val serviceJob: Job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: ${token.take(8)}…")
        serviceScope.launch { pushTokenManager.onTokenRefreshed(token) }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data.toMutableMap()
        // Fallback: if backend ever sends a `notification` block instead of data-only.
        if (data[NotificationDataKeys.TITLE].isNullOrBlank()) {
            remoteMessage.notification?.title?.let { data[NotificationDataKeys.TITLE] = it }
        }
        if (data[NotificationDataKeys.BODY].isNullOrBlank()) {
            remoteMessage.notification?.body?.let { data[NotificationDataKeys.BODY] = it }
        }
        val event = data[NotificationDataKeys.EVENT]
        if (event.isNullOrBlank()) {
            Log.w(TAG, "Incoming FCM has no `event` key — ignoring")
            return
        }

        // Suppress in-room chat notifications when the user is already viewing that room.
        // Realtime broadcast already delivers the message to ChatScreen.
        val isChatEvent = event == NotificationEvents.N_003 || event == NotificationEvents.N_004
        val roomId = data[NotificationDataKeys.ROOM_ID]
        if (isChatEvent && activeRoomTracker.isActive(roomId)) {
            Log.d(TAG, "Suppressing in-room push for active room $roomId")
            return
        }

        NotificationDisplay.show(applicationContext, data)
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TmsFcmService"
    }
}
