package com.teachmeski.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.teachmeski.app.R

object NotificationChannelRegistry {

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.LESSON_REQUESTS,
                context.getString(R.string.notif_channel_lesson_requests),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notif_channel_lesson_requests_desc)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.MESSAGES,
                context.getString(R.string.notif_channel_messages),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notif_channel_messages_desc)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.WALLET,
                context.getString(R.string.notif_channel_wallet),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_wallet_desc)
            },
        )
    }
}
