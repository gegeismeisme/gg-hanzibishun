package com.yourstudio.hskstroke.bishun.hanzi.parser

import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterParserTest {

    private val parser = CharacterParser()

    @Test
    fun `parses strokes and medians into domain model`() {
        val dto = CharacterJsonDto(
            strokes = listOf("M 0 0 L 10 0"),
            medians = listOf(
                listOf(
                    listOf(0f, 0f),
                    listOf(10f, 0f),
                ),
            ),
            radicalStrokes = listOf(0),
        )

        val definition = parser.parse("永", dto)

        assertEquals("永", definition.symbol)
        assertEquals(1, definition.strokeCount)
        val stroke = definition.strokes.first()
        assertEquals("M 0 0 L 10 0", stroke.path)
        assertTrue(stroke.isInRadical)
        assertEquals(2, stroke.points.size)
        assertEquals(10.0, stroke.points.last().x, 1e-6)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when stroke and median counts mismatch`() {
        val dto = CharacterJsonDto(
            strokes = listOf("M 0 0 L 10 0", "M 0 0 L 10 10"),
            medians = listOf(
                listOf(
                    listOf(0f, 0f),
                    listOf(10f, 0f),
                ),
            ),
        )

        parser.parse("永", dto)
    }
}
