package com.yourstudio.hskstroke.bishun.data.daily

import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyPracticeGeneratorTest {

    @Test
    fun suggestSymbol_prefersLowestLevel() {
        val entries = listOf(
            entry(symbol = "一", level = 1, writingLevel = 1),
            entry(symbol = "人", level = 2, writingLevel = 1),
        )

        assertEquals("一", DailyPracticeGenerator.suggestSymbol(entries, completed = emptySet()))
        assertEquals("人", DailyPracticeGenerator.suggestSymbol(entries, completed = setOf("一")))
    }

    @Test
    fun suggestSymbol_ordersByWritingLevelWithinLevel() {
        val entries = listOf(
            entry(symbol = "二", level = 1, writingLevel = 2),
            entry(symbol = "一", level = 1, writingLevel = 1),
        )

        assertEquals("一", DailyPracticeGenerator.suggestSymbol(entries, completed = emptySet()))
    }

    @Test
    fun suggestSymbol_treatsNullWritingLevelAsLast() {
        val entries = listOf(
            entry(symbol = "一", level = 1, writingLevel = null),
            entry(symbol = "二", level = 1, writingLevel = 1),
        )

        assertEquals("二", DailyPracticeGenerator.suggestSymbol(entries, completed = emptySet()))
    }

    @Test
    fun suggestSymbol_trimsSymbolsAndCompletionSet() {
        val entries = listOf(
            entry(symbol = " 一 ", level = 1, writingLevel = 1),
            entry(symbol = "二", level = 1, writingLevel = 2),
        )

        assertEquals("一", DailyPracticeGenerator.suggestSymbol(entries, completed = emptySet()))
        assertEquals("二", DailyPracticeGenerator.suggestSymbol(entries, completed = setOf(" 一 ")))
    }

    private fun entry(symbol: String, level: Int, writingLevel: Int?): HskEntry {
        return HskEntry(
            symbol = symbol,
            level = level,
            writingLevel = writingLevel,
            traditional = "",
            frequency = null,
            examples = "",
        )
    }
}

