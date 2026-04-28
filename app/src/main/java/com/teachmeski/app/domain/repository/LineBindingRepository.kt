package com.teachmeski.app.domain.repository

sealed class LineBindResult {
    object Success : LineBindResult()
    object AlreadyBound : LineBindResult()
    object LineAlreadyUsed : LineBindResult()
    data class Error(val message: String?) : LineBindResult()
}

interface LineBindingRepository {
    /**
     * Returns the LINE Login authorize URL with PKCE, OpenID Connect scopes
     * and a freshly generated state/nonce. Caller must launch the URL in a
     * Custom Tab; the response will arrive at `LineCallbackActivity` via the
     * `https://teachmeski.com/auth/line/callback` App Link.
     */
    fun buildAuthorizeUrl(): String

    /**
     * Completes binding by:
     * 1. Verifying the returned `state` matches the persisted one
     *    (drops it after consumption).
     * 2. POSTing `{code, code_verifier, nonce}` to the
     *    `line-oauth-callback` Edge Function which exchanges the code,
     *    verifies the id_token and writes `line_user_id` on
     *    `instructor_profiles`.
     */
    suspend fun completeBinding(code: String, returnedState: String): LineBindResult
}
