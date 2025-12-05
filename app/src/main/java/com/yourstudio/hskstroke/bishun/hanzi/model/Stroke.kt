package com.yourstudio.hskstroke.bishun.hanzi.model

import com.yourstudio.hskstroke.bishun.hanzi.geometry.Geometry

data class Stroke(
    val path: String,
    val points: List<Point>,
    val strokeNum: Int,
    val isInRadical: Boolean = false,
) {

    init {
        require(points.size >= 2) { "Stroke must contain at least 2 points." }
    }

    fun startingPoint(): Point = points.first()

    fun endingPoint(): Point = points.last()

    fun length(): Double = Geometry.length(points)

    fun vectors(): List<Point> {
        var lastPoint = points.first()
        return points.drop(1).map { current ->
            val vector = Geometry.subtract(current, lastPoint)
            lastPoint = current
            vector
        }
    }

    fun distanceTo(point: Point): Double {
        return points.minOf { Geometry.distance(it, point) }
    }

    fun averageDistance(others: List<Point>): Double {
        if (others.isEmpty()) return 0.0
        val sum = others.sumOf { distanceTo(it) }
        return sum / others.size
    }
}
