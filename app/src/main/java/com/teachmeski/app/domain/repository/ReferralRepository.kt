package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.ReferralInfo
import com.teachmeski.app.util.Resource

interface ReferralRepository {
    /** F-116: the signed-in instructor's own referral code + reward config. */
    suspend fun getReferralInfo(): Resource<ReferralInfo>
}
