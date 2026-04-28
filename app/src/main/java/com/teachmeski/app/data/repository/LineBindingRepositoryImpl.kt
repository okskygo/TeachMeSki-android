package com.teachmeski.app.data.repository

import com.teachmeski.app.BuildConfig
import com.teachmeski.app.data.remote.LineBindingDataSource
import com.teachmeski.app.domain.repository.LineBindResult
import com.teachmeski.app.domain.repository.LineBindingRepository
import com.teachmeski.app.util.LineOAuthState
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineBindingRepositoryImpl @Inject constructor(
    private val dataSource: LineBindingDataSource,
    private val state: LineOAuthState,
) : LineBindingRepository {

    override fun buildAuthorizeUrl(): String {
        val init = state.init()
        val params = listOf(
            "response_type" to "code",
            "client_id" to BuildConfig.LINE_CHANNEL_ID,
            "redirect_uri" to REDIRECT_URI,
            "state" to init.state,
            "scope" to "openid",
            "nonce" to init.nonce,
            "code_challenge" to init.codeChallenge,
            "code_challenge_method" to "S256",
        )
        val query = params.joinToString("&") { (k, v) -> "${enc(k)}=${enc(v)}" }
        return "$AUTHORIZE_BASE?$query"
    }

    override suspend fun completeBinding(
        code: String,
        returnedState: String,
    ): LineBindResult {
        val persisted = state.consume(returnedState)
            ?: return LineBindResult.Error("state_mismatch")

        return try {
            val res = dataSource.callback(
                code = code,
                codeVerifier = persisted.verifier,
                nonce = persisted.nonce,
            )
            when {
                res.success == true -> LineBindResult.Success
                res.error == "already_bound" -> LineBindResult.AlreadyBound
                res.error == "line_already_used" -> LineBindResult.LineAlreadyUsed
                else -> LineBindResult.Error(res.error)
            }
        } catch (e: Exception) {
            LineBindResult.Error(e.message)
        }
    }

    private fun enc(s: String): String = URLEncoder.encode(s, Charsets.UTF_8.name())

    private companion object {
        const val AUTHORIZE_BASE = "https://access.line.me/oauth2/v2.1/authorize"
        const val REDIRECT_URI = "https://teachmeski.com/auth/line/callback"
    }
}
