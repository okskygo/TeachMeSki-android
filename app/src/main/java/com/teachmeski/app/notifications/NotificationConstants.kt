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
