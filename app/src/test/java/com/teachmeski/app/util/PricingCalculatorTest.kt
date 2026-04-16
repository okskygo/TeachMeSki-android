package com.teachmeski.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingCalculatorTest {
    @Test
    fun `half day 1 person costs 110`() {
        assertEquals(110, PricingCalculator.calculateUnlockCost(0.5, 1))
    }

    @Test
    fun `1 day 1 person costs 150`() {
        assertEquals(150, PricingCalculator.calculateUnlockCost(1.0, 1))
    }

    @Test
    fun `3 days 1 person costs 260`() {
        assertEquals(260, PricingCalculator.calculateUnlockCost(3.0, 1))
    }

    @Test
    fun `1 day 2 people costs 170`() {
        assertEquals(170, PricingCalculator.calculateUnlockCost(1.0, 2))
    }

    @Test
    fun `min cost is enforced`() {
        assertEquals(80, PricingCalculator.calculateUnlockCost(0.01, 1))
    }

    @Test
    fun `max cost is enforced`() {
        assertEquals(800, PricingCalculator.calculateUnlockCost(14.0, 10))
    }
}
