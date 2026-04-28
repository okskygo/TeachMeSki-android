package com.teachmeski.app.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Result of a LINE binding round-trip emitted by `LineCallbackActivity`
 * after `MainActivity` re-receives the deep-link Intent.
 *
 * @property kind one of "success", "cancelled", "error"
 * @property errorCode when [kind] == "error", one of "alreadyUsed",
 *   "alreadyBound", "generic"
 */
data class LineBindResultUi(
    val kind: String,
    val errorCode: String? = null,
)

/**
 * In-process pub/sub for LINE binding results.
 *
 * Uses `replay = 1` so a result emitted while the consumer is not yet
 * collecting (e.g. the Activity is being recreated after the Custom
 * Tab returned) is still delivered to the next subscriber.
 */
object LineBindResultBus {
    private val _flow = MutableSharedFlow<LineBindResultUi>(replay = 1)
    val flow = _flow.asSharedFlow()

    suspend fun emit(result: LineBindResultUi) {
        _flow.emit(result)
    }
}
