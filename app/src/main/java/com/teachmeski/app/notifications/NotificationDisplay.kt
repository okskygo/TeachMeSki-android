package com.teachmeski.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.teachmeski.app.R

object NotificationDisplay {

    private const val PREFS_NAME = "tms_notifications"
    private const val KEY_ROOM_UNREAD_PREFIX = "room_unread_"

    @SuppressLint("MissingPermission")
    fun show(context: Context, data: Map<String, String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val event = data[NotificationDataKeys.EVENT] ?: return
        val title = data[NotificationDataKeys.TITLE].orEmpty()
        val body = data[NotificationDataKeys.BODY].orEmpty()
        val channelId = channelForEvent(event)
        val pendingIntent = NotificationDeepLink.buildPendingIntent(context, data)
        val roomId = data[NotificationDataKeys.ROOM_ID]
        val isChatEvent = event == NotificationEvents.N_003 || event == NotificationEvents.N_004

        val notificationId = if (isChatEvent && !roomId.isNullOrBlank()) {
            roomNotificationId(roomId)
        } else {
            NotificationDeepLink.buildRequestCode(data)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notif_default)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (isChatEvent && !roomId.isNullOrBlank()) {
            val newCount = incrementRoomUnread(context, roomId)
            if (newCount > 1) {
                val subText = context.resources.getQuantityString(
                    R.plurals.notif_chat_new_messages_count,
                    newCount,
                    newCount,
                )
                builder.setSubText(subText)
                builder.setNumber(newCount)
            }
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    /**
     * Clears the system notification and in-memory unread counter for a given room.
     * Called when the user opens the chat room.
     */
    fun clearRoomNotifications(context: Context, roomId: String) {
        if (roomId.isBlank()) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(roomNotificationId(roomId))
        prefs(context).edit().remove(KEY_ROOM_UNREAD_PREFIX + roomId).apply()
    }

    private fun roomNotificationId(roomId: String): Int = roomId.hashCode()

    private fun incrementRoomUnread(context: Context, roomId: String): Int {
        val key = KEY_ROOM_UNREAD_PREFIX + roomId
        val p = prefs(context)
        val next = p.getInt(key, 0) + 1
        p.edit().putInt(key, next).apply()
        return next
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun channelForEvent(event: String): String = when (event) {
        NotificationEvents.N_001 -> NotificationChannels.LESSON_REQUESTS
        NotificationEvents.N_002,
        NotificationEvents.N_003,
        NotificationEvents.N_004 -> NotificationChannels.MESSAGES
        NotificationEvents.N_005 -> NotificationChannels.WALLET
        else -> NotificationChannels.MESSAGES
    }
}
