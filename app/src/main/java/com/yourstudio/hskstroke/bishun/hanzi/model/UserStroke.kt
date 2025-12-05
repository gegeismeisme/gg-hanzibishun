package com.yourstudio.hskstroke.bishun.hanzi.model

class UserStroke(
    val id: Int,
    startingPoint: Point,
    startingExternalPoint: Point,
) {
    val points: MutableList<Point> = mutableListOf(startingPoint)
    val externalPoints: MutableList<Point> = mutableListOf(startingExternalPoint)

    fun append(point: Point, externalPoint: Point) {
        points.add(point)
        externalPoints.add(externalPoint)
    }
}
