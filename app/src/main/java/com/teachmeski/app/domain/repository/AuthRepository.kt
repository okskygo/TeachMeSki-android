package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: StateFlow<SessionStatus>
    fun currentUserId(): String?
    fun currentUserEmail(): String?
    suspend fun signUp(email: String, password: String, displayName: String): Resource<Unit>
    suspend fun signIn(email: String, password: String): Resource<Unit>
    suspend fun signOut(): Resource<Unit>
    suspend fun resetPasswordForEmail(email: String): Resource<Unit>
    suspend fun verifyEmailOtp(email: String, token: String): Resource<Unit>
    suspend fun resendEmailOtp(email: String): Resource<Unit>

    /**
     * Pre-signUp existence check. Returns `true` if the email is already
     * registered in `auth.users`. Lets every signUp call site (student
     * Sign Up, instructor wizard) short-circuit instead of falling into
     * Supabase's silent HTTP-200-no-email anti-enumeration response.
     * Errors (network / RPC) bubble up wrapped in [Resource.Error] so
     * callers can decide whether to fail-open.
     */
    suspend fun checkEmailRegistered(email: String): Resource<Boolean>
}
