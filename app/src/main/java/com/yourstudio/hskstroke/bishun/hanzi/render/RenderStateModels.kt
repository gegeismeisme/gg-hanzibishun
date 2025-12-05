package com.example.bishun.hanzi.render

import com.example.bishun.hanzi.model.Point

data class StrokeRenderState(
    val strokeIndex: Int,
    val opacity: Float = 0f,
    val displayPortion: Float = 0f,
) {
    fun interpolate(target: StrokeRenderState, progress: Float): StrokeRenderState {
        val t = progress.coerceIn(0f, 1f)
        return copy(
            opacity = lerp(opacity, target.opacity, t),
            displayPortion = lerp(displayPortion, target.displayPortion, t),
        )
    }
}

data class CharacterRenderState(
    val opacity: Float = 0f,
    val strokes: Map<Int, StrokeRenderState> = emptyMap(),
) {
    fun interpolate(target: CharacterRenderState, progress: Float): CharacterRenderState {
        val t = progress.coerceIn(0f, 1f)
        val mergedStrokes = buildMap {
            putAll(strokes)
            target.strokes.keys.forEach { key ->
                val start = strokes[key]
                val end = target.strokes[key]
                if (start != null && end != null) {
                    put(key, start.interpolate(end, t))
                } else if (end != null) {
                    put(key, end)
                }
            }
        }
        return copy(
            opacity = lerp(opacity, target.opacity, t),
            strokes = mergedStrokes,
        )
    }
}

data class CharacterLayersState(
    val main: CharacterRenderState,
    val outline: CharacterRenderState,
    val highlight: CharacterRenderState,
) {
    fun interpolate(target: CharacterLayersState, progress: Float): CharacterLayersState {
        val t = progress.coerceIn(0f, 1f)
        return CharacterLayersState(
            main = main.interpolate(target.main, t),
            outline = outline.interpolate(target.outline, t),
            highlight = highlight.interpolate(target.highlight, t),
        )
    }
}

data class QuizRenderState(
    val activeUserStrokeId: Int? = null,
)

data class UserStrokeRenderState(
    val id: Int,
    val points: List<Point>,
    val opacity: Float,
)

data class RenderStateSnapshot(
    val options: RenderOptionsState,
    val character: CharacterLayersState,
    val userStrokes: Map<Int, UserStrokeRenderState> = emptyMap(),
    val quiz: QuizRenderState = QuizRenderState(),
) {
    fun interpolate(target: RenderStateSnapshot, progress: Float): RenderStateSnapshot {
        val t = progress.coerceIn(0f, 1f)
        val blendedUserStrokes = userStrokes.mapValues { (id, stroke) ->
            val end = target.userStrokes[id]
            if (end != null) {
                stroke.copy(opacity = lerp(stroke.opacity, end.opacity, t))
            } else {
                stroke.copy(opacity = lerp(stroke.opacity, 0f, t))
            }
        }
        val additionalStrokes = target.userStrokes.filterKeys { it !in blendedUserStrokes.keys }
        return copy(
            character = character.interpolate(target.character, t),
            userStrokes = blendedUserStrokes + additionalStrokes,
            quiz = target.quiz,
        )
    }
}

private fun lerp(start: Float, end: Float, progress: Float): Float {
    return start + (end - start) * progress
}
