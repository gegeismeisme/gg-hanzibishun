package com.example.bishun.hanzi.core

import com.example.bishun.hanzi.model.Point

class Positioner(
    width: Float,
    height: Float,
    padding: Float = 20f,
) {

    private val scale: Double
    private val xOffset: Double
    private val yOffset: Double
    private val canvasHeight: Double = height.toDouble()

    init {
        val paddingD = padding.toDouble()
        val effectiveWidth = (width - 2 * padding).toDouble()
        val effectiveHeight = (height - 2 * padding).toDouble()

        val boundsWidth = CHARACTER_BOUNDS[1].x - CHARACTER_BOUNDS[0].x
        val boundsHeight = CHARACTER_BOUNDS[1].y - CHARACTER_BOUNDS[0].y

        val scaleX = effectiveWidth / boundsWidth
        val scaleY = effectiveHeight / boundsHeight

        scale = minOf(scaleX, scaleY)

        val xCenterBuffer = paddingD + (effectiveWidth - scale * boundsWidth) / 2.0
        val yCenterBuffer = paddingD + (effectiveHeight - scale * boundsHeight) / 2.0

        xOffset = -CHARACTER_BOUNDS[0].x * scale + xCenterBuffer
        yOffset = -CHARACTER_BOUNDS[0].y * scale + yCenterBuffer
    }

    fun convertExternalPoint(point: Point): Point {
        val x = (point.x - xOffset) / scale
        val y = (canvasHeight - yOffset - point.y) / scale
        return Point(x.toDouble(), y.toDouble())
    }

    companion object {
        private val CHARACTER_BOUNDS = arrayOf(
            Point(0.0, -124.0),
            Point(1024.0, 900.0),
        )
    }
}
