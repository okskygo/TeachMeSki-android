package com.teachmeski.app.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.teachmeski.app.MainActivity
import com.teachmeski.app.domain.repository.LineBindResult
import com.teachmeski.app.domain.repository.LineBindingRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Receives the App Link callback from LINE Login at
 * `https://teachmeski.com/auth/line/callback?code=…&state=…` (or
 * `…?error=access_denied`), runs the Edge Function exchange via
 * [LineBindingRepository.completeBinding], then re-launches
 * [MainActivity] with the result encoded as Intent extras so the
 * existing UI picks it up via `LineBindResultBus`.
 *
 * The Activity itself never renders UI — it uses a translucent theme
 * and finishes as soon as the round-trip completes.
 */
@AndroidEntryPoint
class LineCallbackActivity : ComponentActivity() {

    @Inject
    lateinit var repo: LineBindingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent?.data
        val code = data?.getQueryParameter("code")
        val state = data?.getQueryParameter("state")
        val error = data?.getQueryParameter("error")

        if (error == "access_denied") {
            relaunch(EXTRA_KIND_CANCELLED, errorCode = null)
            return
        }
        if (code.isNullOrBlank() || state.isNullOrBlank()) {
            relaunch(EXTRA_KIND_ERROR, errorCode = ERROR_GENERIC)
            return
        }

        lifecycleScope.launch {
            val result = runCatching { repo.completeBinding(code, state) }
                .getOrElse { LineBindResult.Error(it.message) }
            val (kind, errCode) = when (result) {
                LineBindResult.Success -> EXTRA_KIND_SUCCESS to null
                LineBindResult.AlreadyBound -> EXTRA_KIND_ERROR to ERROR_ALREADY_BOUND
                LineBindResult.LineAlreadyUsed -> EXTRA_KIND_ERROR to ERROR_ALREADY_USED
                is LineBindResult.Error -> EXTRA_KIND_ERROR to ERROR_GENERIC
            }
            relaunch(kind, errCode)
        }
    }

    private fun relaunch(kind: String, errorCode: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_LINE_RESULT, kind)
            errorCode?.let { putExtra(EXTRA_LINE_ERROR_CODE, it) }
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_LINE_RESULT = "line_result"
        const val EXTRA_LINE_ERROR_CODE = "line_error_code"

        const val EXTRA_KIND_SUCCESS = "success"
        const val EXTRA_KIND_CANCELLED = "cancelled"
        const val EXTRA_KIND_ERROR = "error"

        const val ERROR_ALREADY_BOUND = "alreadyBound"
        const val ERROR_ALREADY_USED = "alreadyUsed"
        const val ERROR_GENERIC = "generic"
    }
}
