package com.teachmeski.app.notifications

object NotificationChannels {
    const val LESSON_REQUESTS = "tms_lesson_requests"
    const val MESSAGES = "tms_messages"
    const val WALLET = "tms_wallet"
}

object NotificationEvents {
    const val N_001 = "N-001"
    const val N_002 = "N-002"
    const val N_003 = "N-003"
    const val N_004 = "N-004"
    const val N_005 = "N-005"
    /**
     * F-109-N007 / F-113 FR-113-018 #3: lesson-request quota expansion.
     * Routed in `MainActivity#HandleNotificationDeepLinks` to switch to the
     * instructor panel and land on Explore (request detail not yet a
     * standalone route on Android — Explore card surfaces the expanded
     * request).
     */
    const val N_007 = "N-007"

    // TODO(F-113 FR-113-018 #4): IAP-success push event. The
    // `send-push-notification` Edge Function does NOT yet emit a push when
    // a token-pack purchase succeeds (only N-006 = IAP refund is wired).
    // Once an event code (likely "N-008" or "IAP-001") is added server-side,
    // declare the constant here and add the matching `when` branch in
    // `MainActivity#HandleNotificationDeepLinks` (switch to instructor +
    // navigate to `Route.Wallet`).
}

object NotificationDataKeys {
    const val EVENT = "event"
    const val TITLE = "title"
    const val BODY = "body"
    const val ROOM_ID = "room_id"
    const val REQUEST_ID = "request_id"
    const val TRANSACTION_ID = "transaction_id"
}

object NotificationIntentExtras {
    const val EVENT = "tms_notif_event"
    const val ROOM_ID = "tms_notif_room_id"
    const val REQUEST_ID = "tms_notif_request_id"
    const val TRANSACTION_ID = "tms_notif_transaction_id"
}
