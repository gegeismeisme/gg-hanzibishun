package com.yourstudio.hskstroke.bishun.hanzi.model

import kotlin.math.abs

data class Point(
    val x: Double,
    val y: Double,
) {
    fun almostEquals(other: Point, epsilon: Double = 1e-6): Boolean {
        return abs(x - other.x) < epsilon && abs(y - other.y) < epsilon
    }
}
