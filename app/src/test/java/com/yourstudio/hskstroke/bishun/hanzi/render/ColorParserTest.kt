package com.yourstudio.hskstroke.bishun.hanzi.render

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorParserTest {

    @Test
    fun `parses short hex`() {
        val color = ColorParser.parse("#abc")
        assertEquals(0xAA, color.r)
        assertEquals(0xBB, color.g)
        assertEquals(0xCC, color.b)
        assertEquals(1f, color.a, 0.0f)
    }

    @Test
    fun `parses rgba string`() {
        val color = ColorParser.parse("rgba(255, 0, 128, 0.5)")
        assertEquals(255, color.r)
        assertEquals(0, color.g)
        assertEquals(128, color.b)
        assertEquals(0.5f, color.a, 0.0f)
    }
}
