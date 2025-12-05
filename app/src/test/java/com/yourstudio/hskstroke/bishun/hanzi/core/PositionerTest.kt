package com.example.bishun.hanzi.core

import com.example.bishun.hanzi.model.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class PositionerTest {

    @Test
    fun `center point maps near glyph center`() {
        val width = 1064f
        val height = 1064f
        val positioner = Positioner(width = width, height = height, padding = 20f)

        val result = positioner.convertExternalPoint(Point((width / 2).toDouble(), (height / 2).toDouble()))

        assertEquals(512.0, result.x, 1.0)
        assertEquals(388.0, result.y, 1.0)
    }
}
