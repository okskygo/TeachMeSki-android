package com.teachmeski.app.util

object PhoneUtils {

    fun normalizePhone(raw: String): String {
        val digits = raw.replace(Regex("[^+\\d]"), "")

        return when {
            digits.startsWith("09") && digits.length == 10 ->
                "+886${digits.substring(1)}"
            digits.startsWith("+8869") && digits.length == 13 ->
                digits
            digits.startsWith("0") && digits.length == 11 && !digits.startsWith("09") ->
                "+81${digits.substring(1)}"
            digits.startsWith("+81") && digits.length == 13 ->
                digits
            digits.startsWith("+") && digits.length >= 10 ->
                digits
            else -> throw IllegalArgumentException("Unsupported phone format: $raw")
        }
    }

    fun maskPhone(e164: String): String {
        if (e164.length < 6) return e164
        val prefix = e164.substring(0, 4)
        val suffix = e164.takeLast(3)
        val masked = "*".repeat(e164.length - 7)
        return "$prefix $masked $suffix"
    }

    fun getCooldownSeconds(attemptCount: Int): Int = when {
        attemptCount <= 1 -> 60
        attemptCount == 2 -> 120
        attemptCount == 3 -> 240
        attemptCount == 4 -> 480
        else -> 960
    }
}
