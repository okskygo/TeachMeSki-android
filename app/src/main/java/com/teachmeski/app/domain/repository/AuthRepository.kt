package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: StateFlow<SessionStatus>
    fun currentUserId(): String?
    suspend fun signUp(email: String, password: String, displayName: String): Resource<Unit>
    suspend fun signIn(email: String, password: String): Resource<Unit>
    suspend fun signOut(): Resource<Unit>
    suspend fun resetPasswordForEmail(email: String): Resource<Unit>
    suspend fun verifyEmailOtp(email: String, token: String): Resource<Unit>
    suspend fun resendEmailOtp(email: String): Resource<Unit>
}
