package com.teachmeski.app.domain.model

/**
 * F-116: outcome of submitting a referral code during instructor
 * onboarding (`submit_referral_code` RPC). Mirrors iOS
 * `ReferralSubmissionError` (invalidCode/selfReferral/alreadyReferred/generic)
 * with Kotlin enum-constant naming.
 */
enum class ReferralSubmissionError {
    INVALID_CODE,
    SELF_REFERRAL,
    ALREADY_REFERRED,
    GENERIC,
    ;

    companion object {
        /**
         * Maps the `submit_referral_code` RPC's error code (or any thrown
         * exception's message) to a submission error. `no_instructor_profile`
         * and `not_authenticated` — and anything unrecognized — collapse to
         * [GENERIC] since the wizard's post-signup notice only distinguishes
         * the three user-actionable reasons.
         */
        fun fromErrorCode(code: String?): ReferralSubmissionError = when (code) {
            "invalid_code" -> INVALID_CODE
            "self_referral" -> SELF_REFERRAL
            "already_referred" -> ALREADY_REFERRED
            else -> GENERIC
        }
    }
}
