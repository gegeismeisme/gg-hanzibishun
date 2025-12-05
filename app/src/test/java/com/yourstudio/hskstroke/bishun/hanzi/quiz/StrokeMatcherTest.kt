package com.yourstudio.hskstroke.bishun.hanzi.quiz

import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.model.Stroke
import com.yourstudio.hskstroke.bishun.hanzi.model.UserStroke
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrokeMatcherTest {

    private val horizontalStroke = Stroke(
        path = "M 0 0 L 10 0",
        points = listOf(Point(0.0, 0.0), Point(10.0, 0.0)),
        strokeNum = 0,
    )

    private val character = CharacterDefinition("一", listOf(horizontalStroke))

    @Test
    fun `matches forward stroke`() {
        val stroke = UserStroke(1, Point(0.0, 0.0), Point(0.0, 0.0))
        stroke.append(Point(10.0, 0.0), Point(10.0, 0.0))

        val result = StrokeMatcher.matches(stroke, character, 0)

        assertTrue(result.isMatch)
        assertFalse(result.isStrokeBackwards)
    }

    @Test
    fun `flags backwards stroke`() {
        val stroke = UserStroke(2, Point(10.0, 0.0), Point(10.0, 0.0))
        stroke.append(Point(7.5, 0.0), Point(7.5, 0.0))
        stroke.append(Point(5.0, 0.0), Point(5.0, 0.0))
        stroke.append(Point(0.0, 0.0), Point(0.0, 0.0))

        val result = StrokeMatcher.matches(
            stroke,
            character,
            0,
            StrokeMatcher.Options(leniency = 1.5),
        )

        assertFalse(result.isMatch)
        assertTrue("result=$result", result.isStrokeBackwards)
    }
}
