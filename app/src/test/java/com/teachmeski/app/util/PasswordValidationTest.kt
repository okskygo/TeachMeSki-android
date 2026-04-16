package com.teachmeski.app.util

import com.teachmeski.app.ui.component.PasswordRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidationTest {

    @Test
    fun `empty password fails all rules except max`() {
        val rules = PasswordRules.check("")
        assertFalse(rules.hasMinLength)
        assertTrue(rules.hasMaxLength)
        assertFalse(rules.hasUppercase)
        assertFalse(rules.hasLowercase)
        assertFalse(rules.hasDigit)
        assertFalse(rules.allPassed)
    }

    @Test
    fun `valid password passes all rules`() {
        val rules = PasswordRules.check("Test1234")
        assertTrue(rules.allPassed)
    }

    @Test
    fun `password without uppercase fails`() {
        val rules = PasswordRules.check("test1234")
        assertFalse(rules.hasUppercase)
        assertFalse(rules.allPassed)
    }

    @Test
    fun `password without digit fails`() {
        val rules = PasswordRules.check("Testtest")
        assertFalse(rules.hasDigit)
        assertFalse(rules.allPassed)
    }

    @Test
    fun `password too short fails`() {
        val rules = PasswordRules.check("Te1")
        assertFalse(rules.hasMinLength)
        assertFalse(rules.allPassed)
    }

    @Test
    fun `password at 129 chars exceeds max`() {
        val long = "A" + "a".repeat(127) + "1"
        val rules = PasswordRules.check(long)
        assertFalse(rules.hasMaxLength)
        assertFalse(rules.allPassed)
    }
}
