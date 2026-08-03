package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/** F-116: `referral_config` row id=1 snapshot (base + optional campaign reward). */
@Serializable
data class ReferralConfigDto(
    @SerialName("base_reward") val baseReward: Int = 0,
    @SerialName("campaign_reward") val campaignReward: Int? = null,
    @SerialName("campaign_ends_at") val campaignEndsAt: String? = null,
)

@Singleton
class ReferralDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    /** The signed-in instructor's own DB-assigned 6-digit referral code, or null if no profile row. */
    suspend fun getMyReferralCode(userId: String): String? {
        @Serializable
        data class CodeRow(@SerialName("referral_code") val referralCode: String? = null)

        return supabaseClient.postgrest.from("instructor_profiles")
            .select(columns = Columns.list("referral_code")) {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<CodeRow>()
            ?.referralCode
    }

    suspend fun getReferralConfig(): ReferralConfigDto =
        supabaseClient.postgrest.from("referral_config")
            .select(columns = Columns.list("base_reward", "campaign_reward", "campaign_ends_at")) {
                filter { eq("id", 1) }
            }
            .decodeSingle<ReferralConfigDto>()

    /**
     * F-116: submit a referral code entered during instructor onboarding.
     * Throws with the RPC's error code (`invalid_code`/`self_referral`/
     * `already_referred`/`no_instructor_profile`/`not_authenticated`) as the
     * exception message when the RPC reports `{error: ...}`.
     */
    suspend fun submitReferralCode(code: String) {
        val params = buildJsonObject { put("p_code", code) }
        val response = supabaseClient.postgrest.rpc("submit_referral_code", params)
        val payload = response.decodeAs<JsonObject>()
        payload["error"]?.jsonPrimitive?.contentOrNull?.let { error(it) }
    }
}
