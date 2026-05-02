package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email as EmailProvider
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    val sessionStatus: StateFlow<SessionStatus>
        get() = supabaseClient.auth.sessionStatus

    fun currentUserId(): String? =
        supabaseClient.auth.currentSessionOrNull()?.user?.id

    fun currentUserEmail(): String? =
        supabaseClient.auth.currentSessionOrNull()?.user?.email

    suspend fun signUp(email: String, password: String, displayName: String) {
        supabaseClient.auth.signUpWith(EmailProvider) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("display_name", displayName)
                put("role", "student")
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        supabaseClient.auth.signInWith(EmailProvider) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    suspend fun resetPasswordForEmail(email: String) {
        // NOTE: `redirectUrl` is ignored by the recovery email — the URL
        // shown in the email is built by the `send-auth-email` Edge
        // Function, which hardcodes `https://www.teachmeski.com/reset-password`
        // with `token_hash` + `type` query params (verifyOtp flow, no PKCE
        // verifier needed). See gotcha in TEACHMESKI.md §8.
        // We still pass the canonical www URL so it matches the Supabase
        // Dashboard "Redirect URLs" allowlist if GoTrue ever falls back.
        supabaseClient.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "https://www.teachmeski.com/reset-password",
        )
    }

    suspend fun verifyEmailOtp(email: String, token: String) {
        supabaseClient.auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL,
            email = email,
            token = token,
        )
    }

    suspend fun resendEmailOtp(email: String) {
        supabaseClient.auth.resendEmail(
            type = OtpType.Email.SIGNUP,
            email = email,
        )
    }
}
