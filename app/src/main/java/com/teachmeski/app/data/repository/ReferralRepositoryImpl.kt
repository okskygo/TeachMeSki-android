package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.remote.ReferralDataSource
import com.teachmeski.app.domain.model.ReferralInfo
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ReferralRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferralRepositoryImpl @Inject constructor(
    private val referralDataSource: ReferralDataSource,
    private val authRepository: AuthRepository,
) : ReferralRepository {

    override suspend fun getReferralInfo(): Resource<ReferralInfo> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val code = referralDataSource.getMyReferralCode(userId)
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        val config = referralDataSource.getReferralConfig()
        Resource.Success(
            ReferralInfo(
                code = code,
                baseReward = config.baseReward,
                campaignReward = config.campaignReward,
                campaignEndsAt = config.campaignEndsAt?.let(::parseTimestamptz),
            ),
        )
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_generic))
    }
}

/**
 * PostgREST returns `timestamptz` columns either as a plain `Z`-suffixed
 * instant or with a numeric zone offset (e.g. `+00:00`) — `Instant.parse`
 * only accepts the former, so fall back to `OffsetDateTime` for the latter.
 */
private fun parseTimestamptz(raw: String): Instant = try {
    Instant.parse(raw)
} catch (e: DateTimeParseException) {
    OffsetDateTime.parse(raw).toInstant()
}
