package com.teachmeski.app.ui.instructorwizard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * F-116 — `InstructorWizardUiState.canAdvanceFromCurrentStep()` step 5 gate:
 * the (optional) referral code must be either empty or exactly 6 digits,
 * matching the web/iOS validation.
 */
class InstructorWizardUiStateCanAdvanceTest {

    private fun step5State(referralCode: String) =
        InstructorWizardUiState(
            currentStep = 5,
            displayName = "Sam",
            bio = "",
            referralCode = referralCode,
        )

    @Test
    fun `empty referral code allows advancing`() {
        assertTrue(step5State(referralCode = "").canAdvanceFromCurrentStep())
    }

    @Test
    fun `six digit referral code allows advancing`() {
        assertTrue(step5State(referralCode = "012345").canAdvanceFromCurrentStep())
    }

    @Test
    fun `one to five digit referral code blocks advancing`() {
        for (partial in listOf("1", "12", "123", "1234", "12345")) {
            assertFalse(
                "expected referralCode=\"$partial\" to block advancing",
                step5State(referralCode = partial).canAdvanceFromCurrentStep(),
            )
        }
    }
}
