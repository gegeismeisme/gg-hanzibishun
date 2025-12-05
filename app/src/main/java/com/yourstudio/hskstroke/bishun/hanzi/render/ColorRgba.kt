package com.yourstudio.hskstroke.bishun.hanzi.render

import kotlin.math.roundToInt

data class ColorRgba(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Float = 1f,
) {
    init {
        require(r in 0..255 && g in 0..255 && b in 0..255) {
            "RGB values must be between 0 and 255."
        }
        require(a in 0f..1f) { "Alpha must be between 0 and 1." }
    }

    fun lerp(to: ColorRgba, progress: Float): ColorRgba {
        val clamped = progress.coerceIn(0f, 1f)
        if (clamped == 0f) return this
        if (clamped == 1f) return to
        return ColorRgba(
            r = lerpInt(r, to.r, clamped),
            g = lerpInt(g, to.g, clamped),
            b = lerpInt(b, to.b, clamped),
            a = lerpFloat(a, to.a, clamped),
        )
    }

    private fun lerpInt(from: Int, to: Int, progress: Float): Int {
        return (from + (to - from) * progress).roundToInt()
    }

    private fun lerpFloat(from: Float, to: Float, progress: Float): Float {
        return from + (to - from) * progress
    }
}
