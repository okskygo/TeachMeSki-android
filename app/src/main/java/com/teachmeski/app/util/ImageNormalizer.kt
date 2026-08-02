package com.teachmeski.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Normalize a certificate/portfolio image before upload so the payload
 * always matches what the production `avatars` bucket accepts
 * (`image/jpeg` / `image/png` only, 10 MiB cap) — the picker may hand us
 * HEIC or an arbitrarily large original, and the raw pass-through used to
 * fail the upload with a generic error (M-IMG-001).
 *
 * Web reference: `compressImage` in `MentorProfileClient.tsx` with
 * `CERT_MAX_PX = 2048`, `CERT_MAX_BYTES = 4 MiB`. Behavior matches the web
 * client exactly:
 *  - Decode anything the platform can (including HEIC).
 *  - Hard-cap the longest edge at [MAX_DIMENSION] (2048 px).
 *  - Re-encode as JPEG down a quality ladder 85 → 45 (10-point steps),
 *    stopping as soon as the result is ≤ [TARGET_UPLOAD_BYTES] (4 MiB);
 *    final fallback emits quality 40 even if it exceeds the target.
 *  - Always outputs `image/jpeg` (the web client converts PNG too).
 *
 * iOS reference: `Util/ImageNormalizer.swift` (identical constants and
 * ladder). Falls back to the original bytes + content type when decoding
 * fails (defensive — same policy as the avatar compress paths).
 */
object ImageNormalizer {
    const val MAX_DIMENSION = 2048
    const val TARGET_UPLOAD_BYTES = 4 * 1024 * 1024

    class Normalized(val bytes: ByteArray, val contentType: String)

    fun normalizeToJpeg(rawBytes: ByteArray, fallbackContentType: String): Normalized {
        val original = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            ?: return Normalized(rawBytes, fallbackContentType)

        val w = original.width
        val h = original.height
        val maxSide = max(w, h)
        val scaled = if (maxSide > MAX_DIMENSION) {
            val ratio = MAX_DIMENSION.toFloat() / maxSide
            Bitmap.createScaledBitmap(original, (w * ratio).toInt(), (h * ratio).toInt(), true)
        } else {
            original
        }

        var quality = 85
        while (quality >= 40) {
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            val bytes = out.toByteArray()
            if (bytes.size <= TARGET_UPLOAD_BYTES) return Normalized(bytes, "image/jpeg")
            quality -= 10
        }

        val fallback = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 40, fallback)
        return Normalized(fallback.toByteArray(), "image/jpeg")
    }
}
