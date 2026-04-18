package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PhoneAttemptDto(
    @SerialName("user_id") val userId: String,
    @SerialName("last_sent_at") val lastSentAt: String,
    @SerialName("attempt_count") val attemptCount: Int,
)

@Singleton
class PhoneVerificationDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {

    suspend fun isPhoneUnique(e164: String, currentUserId: String): Boolean {
        val result = supabaseClient.postgrest["instructor_profiles"]
            .select {
                filter {
                    eq("phone", e164)
                    neq("user_id", currentUserId)
                }
            }
            .decodeList<PhoneOwnerRow>()
        return result.isEmpty()
    }

    suspend fun getAttempt(userId: String): PhoneAttemptDto? =
        supabaseClient.postgrest["phone_verification_attempts"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<PhoneAttemptDto>()

    suspend fun upsertAttempt(userId: String, attemptCount: Int, lastSentAt: String) {
        supabaseClient.postgrest["phone_verification_attempts"]
            .upsert(
                PhoneAttemptUpsert(
                    userId = userId,
                    attemptCount = attemptCount,
                    lastSentAt = lastSentAt,
                ),
            )
    }

    suspend fun updateUserPhone(e164: String) {
        supabaseClient.auth.updateUser { phone = e164 }
    }

    suspend fun verifyPhoneOtp(phone: String, token: String) {
        supabaseClient.auth.verifyPhoneOtp(
            type = OtpType.Phone.PHONE_CHANGE,
            phone = phone,
            token = token,
        )
    }

    suspend fun confirmVerification(): ConfirmResult {
        val response = supabaseClient.postgrest.rpc("confirm_phone_verification")
            .decodeSingle<JsonObject>()
        val error = response["error"]?.jsonPrimitive?.content
        return if (error != null) {
            ConfirmResult(success = false, error = error)
        } else {
            ConfirmResult(success = true, error = null)
        }
    }

    @Serializable
    private data class PhoneOwnerRow(
        @SerialName("user_id") val userId: String,
    )

    @Serializable
    private data class PhoneAttemptUpsert(
        @SerialName("user_id") val userId: String,
        @SerialName("attempt_count") val attemptCount: Int,
        @SerialName("last_sent_at") val lastSentAt: String,
        @SerialName("updated_at") val updatedAt: String = java.time.Instant.now().toString(),
    )

    data class ConfirmResult(val success: Boolean, val error: String?)
}
