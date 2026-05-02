package com.teachmeski.app.domain.model

/**
 * F-008 v2.0 — `request_unlocks.status` enum.
 *
 * Mirrors the DB CHECK constraint `('active','refunded','completed')`:
 * - `Active`: still-valid unlock (instructor can chat, 48hr deadline pending).
 * - `Refunded`: 48hr passed, student never replied → daily cron auto-credited
 *   tokens back; instructor must re-unlock to chat again.
 * - `Completed`: 48hr passed, student did reply → service deemed complete,
 *   tokens NOT returned, slot still occupied.
 */
enum class UnlockStatus(val raw: String) {
    Active("active"),
    Refunded("refunded"),
    Completed("completed");

    companion object {
        fun fromString(raw: String?): UnlockStatus = when (raw?.lowercase()) {
            "refunded" -> Refunded
            "completed" -> Completed
            else -> Active
        }
    }
}
