package com.yourstudio.hskstroke.bishun.data.hsk

import org.junit.Assert.assertEquals
import org.junit.Test

class HskProgressCalculatorTest {

    @Test
    fun calculateSummary_countsCompletedAndFindsNextTargets() {
        val catalog = mapOf(
            1 to listOf("一", "二", "三"),
            2 to listOf("人"),
        )

        val summary = HskProgressCalculator.calculateSummary(
            completed = setOf("一", "三", " 人 "),
            catalog = catalog,
        )

        assertEquals(2, summary.perLevel[1]?.completed)
        assertEquals(3, summary.perLevel[1]?.total)
        assertEquals("二", summary.nextTargets[1])

        assertEquals(1, summary.perLevel[2]?.completed)
        assertEquals(1, summary.perLevel[2]?.total)
        assertEquals(null, summary.nextTargets[2])
    }
}

