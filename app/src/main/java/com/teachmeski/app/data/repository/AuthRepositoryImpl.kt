package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.remote.AuthDataSource
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus>
        get() = authDataSource.sessionStatus

    override fun currentUserId(): String? = authDataSource.currentUserId()

    override suspend fun signUp(email: String, password: String, displayName: String): Resource<Unit> =
        runAuth { authDataSource.signUp(email, password, displayName) }

    override suspend fun signIn(email: String, password: String): Resource<Unit> =
        runAuth { authDataSource.signIn(email, password) }

    override suspend fun signOut(): Resource<Unit> =
        runAuth { authDataSource.signOut() }

    override suspend fun resetPasswordForEmail(email: String): Resource<Unit> =
        runAuth { authDataSource.resetPasswordForEmail(email) }

    override suspend fun verifyEmailOtp(email: String, token: String): Resource<Unit> =
        runAuth { authDataSource.verifyEmailOtp(email, token) }

    override suspend fun resendEmailOtp(email: String): Resource<Unit> =
        runAuth { authDataSource.resendEmailOtp(email) }

    private suspend fun runAuth(block: suspend () -> Unit): Resource<Unit> =
        try {
            block()
            Resource.Success(Unit)
        } catch (e: RestException) {
            Resource.Error(UiText.StringResource(mapAuthError(e)))
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.auth_error_generic))
        }

    private fun mapAuthError(e: RestException): Int {
        val msg = e.message?.lowercase() ?: ""
        return when {
            "email not confirmed" in msg -> R.string.auth_error_email_not_confirmed
            "invalid login credentials" in msg -> R.string.auth_error_invalid_credentials
            "otp_expired" in msg || "expired" in msg -> R.string.auth_error_otp_expired
            "invalid" in msg && "otp" in msg -> R.string.auth_error_otp_invalid
            else -> R.string.auth_error_generic
        }
    }
}
