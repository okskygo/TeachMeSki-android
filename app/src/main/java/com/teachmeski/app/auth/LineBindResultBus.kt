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
 * `replay = 1` so that a result emitted while the consumer is not yet
 * collecting (e.g. the Activity is being recreated after the Custom
 * Tab returned) is still delivered to the next subscriber.
 *
 * IMPORTANT: subscribers MUST call [consume] after handling each
 * emitted value, otherwise the replay cache keeps re-delivering the
 * same result on every fresh subscription — which manifests as the
 * "Identity verified" toast firing every time the account-settings
 * screen is reopened. This is a singleton object that survives the
 * whole process lifetime, so without explicit consumption the cache
 * lives until the process dies.
 */
object LineBindResultBus {
    private val _flow = MutableSharedFlow<LineBindResultUi>(replay = 1)
    val flow = _flow.asSharedFlow()

    suspend fun emit(result: LineBindResultUi) {
        _flow.emit(result)
    }

    /**
     * Drop the cached replay value so the next subscriber does not
     * re-receive a stale result. Call this from the consumer after
     * the UI side-effect (toast / state update) has been applied.
     */
    fun consume() {
        _flow.resetReplayCache()
    }
}
