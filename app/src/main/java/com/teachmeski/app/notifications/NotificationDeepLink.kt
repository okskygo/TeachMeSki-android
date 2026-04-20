package com.teachmeski.app.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.teachmeski.app.MainActivity

/**
 * Builds a PendingIntent that reopens MainActivity with notification metadata as extras.
 * MainActivity.onNewIntent (and onCreate via getIntent) reads these extras and routes the nav
 * controller to the matching destination.
 */
object NotificationDeepLink {

    fun buildPendingIntent(context: Context, data: Map<String, String>): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationIntentExtras.EVENT, data[NotificationDataKeys.EVENT])
            data[NotificationDataKeys.ROOM_ID]?.let { putExtra(NotificationIntentExtras.ROOM_ID, it) }
            data[NotificationDataKeys.REQUEST_ID]?.let { putExtra(NotificationIntentExtras.REQUEST_ID, it) }
            data[NotificationDataKeys.TRANSACTION_ID]?.let {
                putExtra(NotificationIntentExtras.TRANSACTION_ID, it)
            }
        }
        val requestCode = buildRequestCode(data)
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * Stable per-notification-thread request code so repeated N-003 for the same room replaces
     * the prior PendingIntent instead of stacking up.
     */
    fun buildRequestCode(data: Map<String, String>): Int {
        val event = data[NotificationDataKeys.EVENT] ?: ""
        val key = data[NotificationDataKeys.ROOM_ID]
            ?: data[NotificationDataKeys.REQUEST_ID]
            ?: data[NotificationDataKeys.TRANSACTION_ID]
            ?: ""
        return event.hashCode() xor key.hashCode()
    }
}
