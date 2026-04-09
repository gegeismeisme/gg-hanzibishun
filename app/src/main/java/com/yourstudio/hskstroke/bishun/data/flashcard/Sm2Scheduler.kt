package com.yourstudio.hskstroke.bishun.data.flashcard

import kotlin.math.max

object Sm2Scheduler {

    data class Sm2Result(
        val intervalDays: Int,
        val easeFactor: Double,
        val repetition: Int,
        val nextReviewAtEpochDay: Long,
    )

    fun calculate(
        currentIntervalDays: Int,
        currentEaseFactor: Double,
        currentRepetition: Int,
        quality: Int,
        todayEpochDay: Long,
    ): Sm2Result {
        val normalizedQuality = quality.coerceIn(0, 5)
        var repetition = currentRepetition
        val intervalDays: Int

        if (normalizedQuality < 3) {
            repetition = 0
            intervalDays = 1
        } else {
            repetition += 1
            intervalDays = when (repetition) {
                1 -> 1
                2 -> 6
                else -> max(1, (currentIntervalDays * currentEaseFactor).toInt())
            }
        }

        val qualityDelta = 5 - normalizedQuality
        val nextEaseFactor = (
            currentEaseFactor + (0.1 - qualityDelta * (0.08 + qualityDelta * 0.02))
        ).coerceAtLeast(1.3)

        return Sm2Result(
            intervalDays = intervalDays,
            easeFactor = nextEaseFactor,
            repetition = repetition,
            nextReviewAtEpochDay = todayEpochDay + intervalDays,
        )
    }
}
