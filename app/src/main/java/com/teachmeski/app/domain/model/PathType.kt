package com.teachmeski.app.domain.model

/**
 * F-008 v2.0 — distinguishes Path-A (instructor unlocks an existing
 * lesson_request feed item; subject to 48hr no-reply auto-refund) from
 * Path-B (student initiates a chat with a specific instructor; unlock is
 * non-refundable and `request_unlocks.refund_deadline_at IS NULL`).
 *
 * The signal on `chat_rooms`: `first_instructor_message_at IS NOT NULL`
 * ⇒ Path-A (set by `execute_unlock` when the instructor unlocks a feed
 *   item and the room is freshly created with their first message).
 * Path-B rooms are created by `createPathBChatRoom` (student first message)
 * which leaves `first_instructor_message_at = NULL` until/unless an
 * instructor later unlocks; even after that re-unlock, Path-B unlocks are
 * non-refundable.
 */
enum class PathType { A, B }
