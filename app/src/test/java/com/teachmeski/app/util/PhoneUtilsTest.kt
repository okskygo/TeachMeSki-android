package com.teachmeski.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PhoneUtilsTest {
    @Test
    fun `TW mobile normalizes correctly`() {
        assertEquals("+886912345678", PhoneUtils.normalizePhone("0912345678"))
    }

    @Test
    fun `TW with country code passes through`() {
        assertEquals("+886912345678", PhoneUtils.normalizePhone("+886912345678"))
    }

    @Test
    fun `unsupported format throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            PhoneUtils.normalizePhone("12345")
        }
    }

    @Test
    fun `mask phone hides middle digits`() {
        val masked = PhoneUtils.maskPhone("+886912345678")
        assertEquals("+886 ****** 678", masked)
    }

    @Test
    fun `cooldown schedule is correct`() {
        assertEquals(60, PhoneUtils.getCooldownSeconds(1))
        assertEquals(120, PhoneUtils.getCooldownSeconds(2))
        assertEquals(240, PhoneUtils.getCooldownSeconds(3))
        assertEquals(480, PhoneUtils.getCooldownSeconds(4))
        assertEquals(960, PhoneUtils.getCooldownSeconds(5))
        assertEquals(960, PhoneUtils.getCooldownSeconds(10))
    }
}
