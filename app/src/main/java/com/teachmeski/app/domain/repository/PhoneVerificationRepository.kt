package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource

sealed interface CheckAndSendResult {
    data class Success(val phone: String) : CheckAndSendResult
    data class CooldownBlocked(val seconds: Int) : CheckAndSendResult
    data class Error(val type: String) : CheckAndSendResult
}

sealed interface ConfirmPhoneResult {
    data object Success : ConfirmPhoneResult
    data class Error(val type: String) : ConfirmPhoneResult
}

interface PhoneVerificationRepository {
    suspend fun checkAndSend(rawPhone: String): CheckAndSendResult
    suspend fun verifyOtp(phone: String, token: String): Resource<Unit>
    suspend fun confirm(): ConfirmPhoneResult
}
