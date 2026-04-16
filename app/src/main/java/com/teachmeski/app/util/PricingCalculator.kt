package com.teachmeski.app.util

import kotlin.math.roundToInt
import kotlin.math.sqrt

object PricingCalculator {
    private const val BASE = 150
    private const val GROUP_RATE = 0.1
    private const val MIN_COST = 80
    private const val MAX_COST = 800

    fun calculateUnlockCost(durationDays: Double, groupSize: Int): Int {
        val raw = BASE * sqrt(durationDays) * (1 + GROUP_RATE * (groupSize - 1))
        val rounded = (raw / 10).roundToInt() * 10
        return rounded.coerceIn(MIN_COST, MAX_COST)
    }
}
