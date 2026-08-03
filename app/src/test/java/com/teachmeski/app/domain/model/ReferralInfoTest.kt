package com.teachmeski.app.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ReferralInfoTest {
    private val ends = Instant.parse("2026-12-31T15:59:59Z")

    private fun info(campaign: Int? = 500, endsAt: Instant? = ends) =
        ReferralInfo(code = "012345", baseReward = 100, campaignReward = campaign, campaignEndsAt = endsAt)

    @Test
    fun `campaign active before deadline`() {
        assertEquals(500, info().currentReward(Instant.parse("2026-08-03T00:00:00Z")))
    }

    @Test
    fun `falls back to base after deadline`() {
        assertEquals(100, info().currentReward(Instant.parse("2027-01-01T00:00:00Z")))
    }

    @Test
    fun `null campaign reward means base`() {
        assertEquals(100, info(campaign = null).currentReward(Instant.parse("2026-08-03T00:00:00Z")))
    }

    @Test
    fun `null deadline means open-ended campaign`() {
        assertEquals(500, info(endsAt = null).currentReward(Instant.parse("2030-01-01T00:00:00Z")))
    }
}
