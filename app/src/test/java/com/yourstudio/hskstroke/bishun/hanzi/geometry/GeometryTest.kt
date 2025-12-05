package com.yourstudio.hskstroke.bishun.hanzi.geometry

import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryTest {

    @Test
    fun `length sums individual segments`() {
        val points = listOf(
            Point(0.0, 0.0),
            Point(3.0, 4.0), // length 5
            Point(6.0, 4.0), // length 3
        )

        val length = Geometry.length(points)

        assertEquals(8.0, length, 1e-6)
    }

    @Test
    fun `frechet distance is near zero for matching curves`() {
        val curve1 = listOf(
            Point(0.0, 0.0),
            Point(1.0, 1.0),
            Point(2.0, 2.0),
        )
        val curve2 = listOf(
            Point(0.1, -0.05),
            Point(1.05, 1.0),
            Point(1.95, 2.1),
        )

        val dist = Geometry.frechetDist(curve1, curve2)

        assertTrue("Expected curves to be similar, distance=$dist", dist < 0.3)
    }

    @Test
    fun `outline curve produces requested number of points`() {
        val curve = listOf(
            Point(0.0, 0.0),
            Point(1.0, 0.0),
            Point(2.0, 0.0),
        )

        val outlined = Geometry.outlineCurve(curve, numPoints = 10)

        assertEquals(10, outlined.size)
    }

    @Test
    fun `extend start pushes the first point backwards`() {
        val points = listOf(Point(0.0, 0.0), Point(10.0, 0.0))

        val extended = Geometry.extendStart(points, dist = 5.0)

        assertEquals(-5.0, extended.first().x, 1e-6)
        assertEquals(points.last(), extended.last())
    }

    @Test
    fun `rotate spins curve around origin`() {
        val curve = listOf(Point(1.0, 0.0))

        val rotated = Geometry.rotate(curve, theta = Math.PI / 2)

        assertTrue(abs(rotated.first().x) < 1e-6)
        assertEquals(1.0, rotated.first().y, 1e-6)
    }
}
