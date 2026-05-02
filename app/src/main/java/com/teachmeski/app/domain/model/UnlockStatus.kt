package com.teachmeski.app.domain.model

/**
 * F-008 v2.0 — `request_unlocks.status` enum, plus the client-side
 * synthetic state `Pending` introduced by the 2026-05-02 "我的案件 /
 * My Cases" rename (PRD §3.6 FR-008-053).
 *
 * Mirrors the DB CHECK constraint `('active','refunded','completed')`:
 * - `Active`: still-valid unlock (instructor can chat, 48hr deadline pending).
 * - `Refunded`: 48hr passed, student never replied → daily cron auto-credited
 *   tokens back; instructor must re-unlock to chat again.
 * - `Completed`: 48hr passed, student did reply → service deemed complete,
 *   tokens NOT returned, slot still occupied.
 * - `Pending`: NOT a DB value. Synthesised when a `chat_rooms` row exists
 *   for this (instructor, lesson_request) pair but no matching
 *   `request_unlocks` row — typical Path-B "student opened a room and
 *   coach hasn't paid to unlock yet". Never round-trips to the DB.
 */
enum class UnlockStatus(val raw: String) {
    Active("active"),
    Refunded("refunded"),
    Completed("completed"),
    Pending("pending");

    companion object {
        fun fromString(raw: String?): UnlockStatus = when (raw?.lowercase()) {
            "refunded" -> Refunded
            "completed" -> Completed
            "pending" -> Pending
            "active" -> Active
            // FR-008-053: when the unlock row is absent, fall back to
            // Pending — the chat_room exists but no payment has been
            // made yet. Previously this returned Active which caused
            // Path-B rooms to be falsely shown as "unlocked" on Android.
            null -> Pending
            else -> Active
        }
    }
}
