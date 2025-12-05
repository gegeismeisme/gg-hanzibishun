package com.example.bishun.hanzi.quiz

import com.example.bishun.hanzi.core.average
import com.example.bishun.hanzi.geometry.Geometry
import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Point
import com.example.bishun.hanzi.model.Stroke
import com.example.bishun.hanzi.model.UserStroke

object StrokeMatcher {
    private const val COSINE_SIMILARITY_THRESHOLD = 0.0
    private const val START_END_THRESHOLD = 250.0
    private const val FRECHET_THRESHOLD = 0.4
    private const val MIN_LEN_THRESHOLD = 0.35
    private val SHAPE_FIT_ROTATIONS = listOf(
        Math.PI / 16,
        Math.PI / 32,
        0.0,
        (-1 * Math.PI) / 32,
        (-1 * Math.PI) / 16,
    )

    data class Options(
        val leniency: Double = 1.0,
        val isOutlineVisible: Boolean = false,
        val averageDistanceThreshold: Double = 350.0,
    )

    data class Result(
        val isMatch: Boolean,
        val isStrokeBackwards: Boolean,
    )

    fun matches(
        userStroke: UserStroke,
        character: CharacterDefinition,
        strokeIndex: Int,
        options: Options = Options(),
    ): Result {
        val points = stripDuplicates(userStroke.points)
        if (points.size < 2) {
            return Result(isMatch = false, isStrokeBackwards = false)
        }

        val currentStroke = character.strokes[strokeIndex]
        val matchData = getMatchData(points, currentStroke, options)
        if (!matchData.isMatch) {
            return Result(matchData.isMatch, matchData.meta.isStrokeBackwards)
        }

        val laterStrokes = character.strokes.drop(strokeIndex + 1)
        var closestMatchDist = matchData.avgDist

        for (stroke in laterStrokes) {
            val otherMatch = getMatchData(points, stroke, options, checkBackwards = false)
            if (otherMatch.isMatch && otherMatch.avgDist < closestMatchDist) {
                closestMatchDist = otherMatch.avgDist
            }
        }

        if (closestMatchDist < matchData.avgDist) {
            val leniencyAdjustment =
                (0.6 * (closestMatchDist + matchData.avgDist)) / (2 * matchData.avgDist)
            val adjustedMatch = getMatchData(
                points,
                currentStroke,
                options.copy(leniency = options.leniency * leniencyAdjustment),
            )
            return Result(adjustedMatch.isMatch, adjustedMatch.meta.isStrokeBackwards)
        }

        return Result(matchData.isMatch, matchData.meta.isStrokeBackwards)
    }

    private fun getMatchData(
        points: List<Point>,
        stroke: Stroke,
        options: Options,
        checkBackwards: Boolean = true,
    ): MatchData {
        val avgDist = stroke.averageDistance(points)
        val distMod = if (options.isOutlineVisible || stroke.strokeNum > 0) 0.5 else 1.0
        val withinThreshold =
            avgDist <= options.averageDistanceThreshold * distMod * options.leniency
        if (!withinThreshold) {
            return MatchData(false, avgDist, StrokeMatchMeta(false))
        }
        val startEndMatch = startAndEndMatches(points, stroke, options.leniency)
        val directionMatch = directionMatches(points, stroke)
        val shapeMatch = shapeFit(points, stroke.points, options.leniency)
        val lengthMatch = lengthMatches(points, stroke, options.leniency)
        val isMatch = withinThreshold && startEndMatch && directionMatch && shapeMatch && lengthMatch
        if (checkBackwards && !isMatch) {
            val reversedMatch = getMatchData(
                points.reversed(),
                stroke,
                options,
                checkBackwards = false,
            )
            if (reversedMatch.isMatch) {
                return MatchData(isMatch, avgDist, StrokeMatchMeta(isStrokeBackwards = true))
            }
        }
        return MatchData(isMatch, avgDist, StrokeMatchMeta(false))
    }

    private fun startAndEndMatches(points: List<Point>, stroke: Stroke, leniency: Double): Boolean {
        val startDist = Geometry.distance(stroke.startingPoint(), points.first())
        val endDist = Geometry.distance(stroke.endingPoint(), points.last())
        return startDist <= START_END_THRESHOLD * leniency &&
            endDist <= START_END_THRESHOLD * leniency
    }

    private fun directionMatches(points: List<Point>, stroke: Stroke): Boolean {
        val edgeVectors = getEdgeVectors(points)
        val strokeVectors = stroke.vectors()
        val similarities = edgeVectors.map { edge ->
            val strokeSimilarities = strokeVectors.map { vector ->
                Geometry.cosineSimilarity(vector, edge)
            }
            strokeSimilarities.maxOrNull() ?: 0.0
        }
        val avgSimilarity = average(similarities)
        return avgSimilarity > COSINE_SIMILARITY_THRESHOLD
    }

    private fun lengthMatches(points: List<Point>, stroke: Stroke, leniency: Double): Boolean {
        val userLength = Geometry.length(points)
        val targetLength = stroke.length()
        return (leniency * (userLength + 25)) / (targetLength + 25) >= MIN_LEN_THRESHOLD
    }

    private fun getEdgeVectors(points: List<Point>): List<Point> {
        val vectors = mutableListOf<Point>()
        var last = points.first()
        points.drop(1).forEach { point ->
            vectors.add(Geometry.subtract(point, last))
            last = point
        }
        return vectors
    }

    private fun stripDuplicates(points: List<Point>): List<Point> {
        if (points.size < 2) return points
        val deduped = mutableListOf(points.first())
        points.drop(1).forEach { point ->
            if (!Geometry.equals(point, deduped.last())) {
                deduped.add(point)
            }
        }
        return deduped
    }

    private fun shapeFit(curve1: List<Point>, curve2: List<Point>, leniency: Double): Boolean {
        val normCurve1 = Geometry.normalizeCurve(curve1)
        val normCurve2 = Geometry.normalizeCurve(curve2)
        var minDist = Double.POSITIVE_INFINITY
        SHAPE_FIT_ROTATIONS.forEach { theta ->
            val rotated = Geometry.rotate(normCurve2, theta)
            val dist = Geometry.frechetDist(normCurve1, rotated)
            if (dist < minDist) {
                minDist = dist
            }
        }
        return minDist <= FRECHET_THRESHOLD * leniency
    }

    private data class StrokeMatchMeta(val isStrokeBackwards: Boolean)

    private data class MatchData(
        val isMatch: Boolean,
        val avgDist: Double,
        val meta: StrokeMatchMeta,
    )
}
