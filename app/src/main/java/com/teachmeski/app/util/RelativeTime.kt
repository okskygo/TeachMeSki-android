package com.teachmeski.app.util

import android.content.Context
import com.teachmeski.app.R
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object RelativeTime {
    fun format(isoTimestamp: String?, context: Context): String {
        if (isoTimestamp.isNullOrBlank()) return ""
        return try {
            val parsed = OffsetDateTime.parse(isoTimestamp)
            val now = OffsetDateTime.now()
            val minutes = ChronoUnit.MINUTES.between(parsed, now)
            val days = ChronoUnit.DAYS.between(parsed.toLocalDate(), now.toLocalDate())

            when {
                minutes < 1 -> context.getString(R.string.chat_time_just_now)
                minutes < 60 -> context.getString(R.string.chat_time_minutes_ago_fmt, minutes.toInt())
                days == 0L -> {
                    val timeStr = parsed.atZoneSameInstant(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                    context.getString(R.string.chat_time_today_fmt, timeStr)
                }
                days == 1L -> context.getString(R.string.chat_time_yesterday)
                else -> {
                    val dateStr = parsed.atZoneSameInstant(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MM/dd"))
                    context.getString(R.string.chat_time_date_fmt, dateStr)
                }
            }
        } catch (_: Exception) {
            isoTimestamp.take(10)
        }
    }
}
