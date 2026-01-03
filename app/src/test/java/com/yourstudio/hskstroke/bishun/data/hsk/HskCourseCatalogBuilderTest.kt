package com.yourstudio.hskstroke.bishun.data.hsk

import org.junit.Assert.assertEquals
import org.junit.Test

class HskCourseCatalogBuilderTest {

    @Test
    fun build_groupsByLevelAndSortsByWritingLevel() {
        val entries = listOf(
            entry(symbol = "二", level = 1, writingLevel = 2),
            entry(symbol = "一", level = 1, writingLevel = 1),
            entry(symbol = "人", level = 2, writingLevel = 1),
        )

        val catalog = HskCourseCatalogBuilder.build(entries)
        assertEquals(listOf("一", "二"), catalog[1])
        assertEquals(listOf("人"), catalog[2])
    }

    @Test
    fun build_trimsSymbolsAndSkipsBlank() {
        val entries = listOf(
            entry(symbol = " 一 ", level = 1, writingLevel = 1),
            entry(symbol = "   ", level = 1, writingLevel = 2),
            entry(symbol = "二", level = 1, writingLevel = 3),
        )

        val catalog = HskCourseCatalogBuilder.build(entries)
        assertEquals(listOf("一", "二"), catalog[1])
    }

    @Test
    fun build_treatsNullWritingLevelAsLast() {
        val entries = listOf(
            entry(symbol = "一", level = 1, writingLevel = null),
            entry(symbol = "二", level = 1, writingLevel = 1),
        )

        val catalog = HskCourseCatalogBuilder.build(entries)
        assertEquals(listOf("二", "一"), catalog[1])
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

