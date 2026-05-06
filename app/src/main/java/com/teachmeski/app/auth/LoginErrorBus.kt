package com.teachmeski.app.auth

import com.teachmeski.app.util.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * In-process pub/sub for one-shot login-screen errors that originate
 * outside the login flow itself.
 *
 * Use case: `MainViewModel.resolveRole()` detects a soft-deleted account
 * after sign-in succeeds, force-signs-out, and needs the next
 * `LoginScreen` instance to surface "此帳號已被刪除…".
 *
 * `replay = 1` so the value emitted while the consumer is not yet
 * collecting (LoginViewModel hasn't been constructed yet because
 * AppNavGraph is still re-rooting from authenticated → auth graph) is
 * still delivered. Subscribers MUST call [consume] after applying the
 * error so a stale value is not re-delivered if the user backgrounds
 * and returns to the login screen.
 */
object LoginErrorBus {
    private val _flow = MutableSharedFlow<UiText>(replay = 1)
    val flow = _flow.asSharedFlow()

    suspend fun emit(error: UiText) {
        _flow.emit(error)
    }

    fun consume() {
        _flow.resetReplayCache()
    }
}
