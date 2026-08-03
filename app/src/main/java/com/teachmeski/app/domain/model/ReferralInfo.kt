package com.teachmeski.app.domain.model

import java.time.Instant

/**
 * F-116: the signed-in instructor's own referral code plus the current
 * reward configuration snapshot (`referral_config` row id=1).
 *
 * Mirrors iOS `ReferralInfo` 1:1 — [isCampaignActive] intentionally has
 * no `campaignReward > 0` guard (that was an iOS bug, since fixed).
 */
data class ReferralInfo(
    val code: String,
    val baseReward: Int,
    val campaignReward: Int?,
    val campaignEndsAt: Instant?,
) {
    fun isCampaignActive(now: Instant = Instant.now()): Boolean =
        campaignReward != null && (campaignEndsAt == null || !now.isAfter(campaignEndsAt))

    fun currentReward(now: Instant = Instant.now()): Int =
        if (isCampaignActive(now)) requireNotNull(campaignReward) else baseReward
}
