package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.remote.AuthDataSource
import com.teachmeski.app.data.remote.PhoneVerificationDataSource
import com.teachmeski.app.domain.repository.CheckAndSendResult
import com.teachmeski.app.domain.repository.ConfirmPhoneResult
import com.teachmeski.app.domain.repository.PhoneVerificationRepository
import com.teachmeski.app.util.PhoneUtils
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneVerificationRepositoryImpl @Inject constructor(
    private val dataSource: PhoneVerificationDataSource,
    private val authDataSource: AuthDataSource,
) : PhoneVerificationRepository {

    override suspend fun checkAndSend(rawPhone: String): CheckAndSendResult {
        val e164 = try {
            PhoneUtils.normalizePhone(rawPhone)
        } catch (_: IllegalArgumentException) {
            return CheckAndSendResult.Error("invalid_format")
        }

        val userId = authDataSource.currentUserId()
            ?: return CheckAndSendResult.Error("unauthorized")

        return try {
            val isUnique = dataSource.isPhoneUnique(e164, userId)
            if (!isUnique) return CheckAndSendResult.Error("phone_taken")

            val attempt = dataSource.getAttempt(userId)
            if (attempt != null) {
                val cooldown = PhoneUtils.getCooldownSeconds(attempt.attemptCount)
                val elapsedSec = (Instant.now().epochSecond -
                    Instant.parse(attempt.lastSentAt).epochSecond)
                if (elapsedSec < cooldown) {
                    return CheckAndSendResult.CooldownBlocked(
                        (cooldown - elapsedSec).toInt(),
                    )
                }
            }

            val newCount = (attempt?.attemptCount ?: 0) + 1
            dataSource.upsertAttempt(
                userId = userId,
                attemptCount = newCount,
                lastSentAt = Instant.now().toString(),
            )

            dataSource.updateUserPhone(e164)

            CheckAndSendResult.Success(e164)
        } catch (e: Exception) {
            CheckAndSendResult.Error("unknown")
        }
    }

    override suspend fun verifyOtp(phone: String, token: String): Resource<Unit> =
        try {
            dataSource.verifyPhoneOtp(phone, token)
            Resource.Success(Unit)
        } catch (_: Exception) {
            Resource.Error(UiText.StringResource(R.string.phone_error_otp))
        }

    override suspend fun confirm(): ConfirmPhoneResult =
        try {
            val result = dataSource.confirmVerification()
            if (result.success) {
                ConfirmPhoneResult.Success
            } else {
                ConfirmPhoneResult.Error(result.error ?: "unknown")
            }
        } catch (_: Exception) {
            ConfirmPhoneResult.Error("unknown")
        }
}
