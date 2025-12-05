package com.yourstudio.hskstroke.bishun.hanzi.geometry

import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object Geometry {

    fun subtract(p1: Point, p2: Point): Point = Point(p1.x - p2.x, p1.y - p2.y)

    fun magnitude(point: Point): Double = sqrt(point.x * point.x + point.y * point.y)

    fun distance(point1: Point, point2: Point): Double = magnitude(subtract(point1, point2))

    fun equals(point1: Point, point2: Point): Boolean = point1.x == point2.x && point1.y == point2.y

    fun round(point: Point, precision: Int = 1): Point {
        val multiplier = precision * 10.0
        return Point(
            round(point.x * multiplier) / multiplier,
            round(point.y * multiplier) / multiplier,
        )
    }

    fun length(points: List<Point>): Double {
        if (points.size < 2) return 0.0
        var lastPoint = points.first()
        var sum = 0.0
        points.drop(1).forEach { point ->
            sum += distance(point, lastPoint)
            lastPoint = point
        }
        return sum
    }

    fun cosineSimilarity(point1: Point, point2: Point): Double {
        val denom = magnitude(point1) * magnitude(point2)
        if (denom == 0.0) return 0.0
        val rawDotProduct = point1.x * point2.x + point1.y * point2.y
        return rawDotProduct / denom
    }

    private fun extendPointOnLine(p1: Point, p2: Point, dist: Double): Point {
        val vector = subtract(p2, p1)
        val norm = if (vector.x == 0.0 && vector.y == 0.0) 0.0 else dist / magnitude(vector)
        return Point(
            p2.x + norm * vector.x,
            p2.y + norm * vector.y,
        )
    }

    /**
     * Calculates the discrete Fréchet distance between two polylines.
     * Ported from the reference implementation in the original Hanzi Writer project.
     */
    fun frechetDist(curve1: List<Point>, curve2: List<Point>): Double {
        require(curve1.isNotEmpty() && curve2.isNotEmpty()) {
            "Curves must contain points."
        }
        val (longCurve, shortCurve) =
            if (curve1.size >= curve2.size) curve1 to curve2 else curve2 to curve1

        var previousColumn = DoubleArray(shortCurve.size)
        for (i in longCurve.indices) {
            val currentColumn = DoubleArray(shortCurve.size)
            for (j in shortCurve.indices) {
                currentColumn[j] = calcFrechetValue(
                    i = i,
                    j = j,
                    longCurve = longCurve,
                    shortCurve = shortCurve,
                    prevResultsCol = previousColumn,
                    curResultsCol = currentColumn,
                )
            }
            previousColumn = currentColumn
        }
        return previousColumn[shortCurve.size - 1]
    }

    private fun calcFrechetValue(
        i: Int,
        j: Int,
        longCurve: List<Point>,
        shortCurve: List<Point>,
        prevResultsCol: DoubleArray,
        curResultsCol: DoubleArray,
    ): Double {
        if (i == 0 && j == 0) {
            return distance(longCurve[0], shortCurve[0])
        }
        if (i > 0 && j == 0) {
            return max(prevResultsCol[0], distance(longCurve[i], shortCurve[0]))
        }
        val lastResult = curResultsCol[max(0, j - 1)]
        if (i == 0 && j > 0) {
            return max(lastResult, distance(longCurve[0], shortCurve[j]))
        }
        val option = minOf(prevResultsCol[j], prevResultsCol[j - 1], lastResult)
        return max(option, distance(longCurve[i], shortCurve[j]))
    }

    fun subdivideCurve(curve: List<Point>, maxLen: Double = 0.05): List<Point> {
        if (curve.isEmpty()) return emptyList()
        val newCurve = mutableListOf(curve.first())
        curve.drop(1).forEach { point ->
            val prevPoint = newCurve.last()
            val segLen = distance(point, prevPoint)
            if (segLen > maxLen) {
                val numNewPoints = ceil(segLen / maxLen).toInt()
                val newSegLen = segLen / numNewPoints
                repeat(numNewPoints) { index ->
                    newCurve.add(
                        extendPointOnLine(point, prevPoint, -1.0 * newSegLen * (index + 1)),
                    )
                }
            } else {
                newCurve.add(point)
            }
        }
        return newCurve
    }

    fun outlineCurve(curve: List<Point>, numPoints: Int = 30): List<Point> {
        if (curve.isEmpty()) return emptyList()
        if (curve.size == 1) return curve

        val curveLen = length(curve)
        if (curveLen == 0.0) return List(numPoints) { curve.first() }

        val segmentLen = curveLen / (numPoints - 1)
        val outlinePoints = mutableListOf(curve.first())
        val endPoint = curve.last()
        val remainingCurvePoints = curve.drop(1).toMutableList()

        repeat(numPoints - 2) {
            var lastPoint = outlinePoints.last()
            var remainingDist = segmentLen
            var found = false
            while (!found && remainingCurvePoints.isNotEmpty()) {
                val nextPoint = remainingCurvePoints.first()
                val nextPointDist = distance(lastPoint, nextPoint)
                if (nextPointDist < remainingDist) {
                    remainingDist -= nextPointDist
                    lastPoint = remainingCurvePoints.removeAt(0)
                } else {
                    val newPoint = extendPointOnLine(lastPoint, nextPoint, remainingDist - nextPointDist)
                    outlinePoints.add(newPoint)
                    found = true
                }
            }
        }
        outlinePoints.add(endPoint)
        return outlinePoints
    }

    fun normalizeCurve(curve: List<Point>): List<Point> {
        if (curve.isEmpty()) return emptyList()
        val outlinedCurve = outlineCurve(curve)
        val meanX = outlinedCurve.map { it.x }.average()
        val meanY = outlinedCurve.map { it.y }.average()
        val mean = Point(meanX, meanY)
        val translated = outlinedCurve.map { subtract(it, mean) }
        val first = translated.first()
        val last = translated.last()
        val scale = sqrt(
            average(
                listOf(first.x.pow(2) + first.y.pow(2), last.x.pow(2) + last.y.pow(2)),
            ),
        )
        val scaled = translated.map { point ->
            if (scale == 0.0) Point(0.0, 0.0) else Point(point.x / scale, point.y / scale)
        }
        return subdivideCurve(scaled)
    }

    fun rotate(curve: List<Point>, theta: Double): List<Point> {
        return curve.map { point ->
            Point(
                x = cos(theta) * point.x - sin(theta) * point.y,
                y = sin(theta) * point.x + cos(theta) * point.y,
            )
        }
    }

    private fun filterParallelPoints(points: List<Point>): List<Point> {
        if (points.size < 3) return points
        val filtered = mutableListOf(points[0], points[1])
        points.drop(2).forEach { point ->
            val size = filtered.size
            val curVect = subtract(point, filtered[size - 1])
            val prevVect = subtract(filtered[size - 1], filtered[size - 2])
            val isParallel = curVect.y * prevVect.x - curVect.x * prevVect.y == 0.0
            if (isParallel) {
                filtered.removeAt(filtered.lastIndex)
            }
            filtered.add(point)
        }
        return filtered
    }

    fun getPathString(points: List<Point>, close: Boolean = false): String {
        if (points.isEmpty()) return ""
        val start = round(points.first())
        val sb = StringBuilder("M ${start.x} ${start.y}")
        points.drop(1).forEach { point ->
            val rounded = round(point)
            sb.append(" L ${rounded.x} ${rounded.y}")
        }
        if (close) {
            sb.append("Z")
        }
        return sb.toString()
    }

    fun extendStart(points: List<Point>, dist: Double): List<Point> {
        if (points.isEmpty()) return emptyList()
        val filtered = filterParallelPoints(points)
        if (filtered.size < 2) return filtered
        val p1 = filtered[1]
        val p2 = filtered[0]
        val newStart = extendPointOnLine(p1, p2, dist)
        return buildList {
            add(newStart)
            addAll(filtered.drop(1))
        }
    }

    private fun average(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        return values.sum() / values.size
    }
}
