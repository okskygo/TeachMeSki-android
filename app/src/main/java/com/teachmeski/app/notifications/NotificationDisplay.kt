package com.teachmeski.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.teachmeski.app.R

object NotificationDisplay {

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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notif_default)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationDeepLink.buildRequestCode(data), notification)
    }

    private fun channelForEvent(event: String): String = when (event) {
        NotificationEvents.N_001 -> NotificationChannels.LESSON_REQUESTS
        NotificationEvents.N_002,
        NotificationEvents.N_003,
        NotificationEvents.N_004 -> NotificationChannels.MESSAGES
        NotificationEvents.N_005 -> NotificationChannels.WALLET
        else -> NotificationChannels.MESSAGES
    }
}
