package com.example.bishun.hanzi.render.actions

import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Point
import com.example.bishun.hanzi.render.CharacterLayersState
import com.example.bishun.hanzi.render.CharacterRenderState
import com.example.bishun.hanzi.render.ColorRgba
import com.example.bishun.hanzi.render.Mutation
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.hanzi.render.StrokeRenderState
import com.example.bishun.hanzi.render.UserStrokeRenderState

object QuizActions {

    fun startQuiz(
        character: CharacterDefinition,
        fadeDurationMillis: Long,
        startStrokeNum: Int,
    ): List<Mutation> {
        val hideMain = CharacterActions.hideCharacter(CharacterLayer.MAIN, character, fadeDurationMillis)
        val setupHighlight = Mutation(
            scope = "character.highlight",
            force = true,
            reducer = { state ->
                state.updateLayer(CharacterLayer.HIGHLIGHT) {
                    it.copy(
                        opacity = 1f,
                        strokes = character.strokes.associate { stroke ->
                            stroke.strokeNum to it.strokes[stroke.strokeNum].orDefault(stroke.strokeNum)
                                .copy(opacity = 0f, displayPortion = 0f)
                        },
                    )
                }
            },
        )
        val revealPrevious = Mutation(
            scope = "character.main",
            force = true,
            reducer = { state ->
                state.updateLayer(CharacterLayer.MAIN) {
                    it.copy(
                        opacity = 1f,
                        strokes = character.strokes.associate { stroke ->
                            val alreadyDrawn = stroke.strokeNum < startStrokeNum
                            stroke.strokeNum to StrokeRenderState(
                                strokeIndex = stroke.strokeNum,
                                opacity = if (alreadyDrawn) 1f else 0f,
                                displayPortion = if (alreadyDrawn) 1f else 0f,
                            )
                        },
                    )
                }
            },
        )
        return hideMain + listOf(setupHighlight, revealPrevious)
    }

    fun startUserStroke(id: Int, point: Point): List<Mutation> {
        val setActive = Mutation(
            scope = "quiz.activeUserStrokeId",
            force = true,
            reducer = { state ->
                state.copy(quiz = state.quiz.copy(activeUserStrokeId = id))
            },
        )
        val registerStroke = Mutation(
            scope = "userStrokes.$id",
            force = true,
            reducer = { state ->
                state.copy(
                    userStrokes = state.userStrokes + (id to UserStrokeRenderState(id, listOf(point), 1f)),
                )
            },
        )
        return listOf(setActive, registerStroke)
    }

    fun updateUserStroke(id: Int, points: List<Point>): List<Mutation> {
        return listOf(
            Mutation(
                scope = "userStrokes.$id.points",
                force = true,
                reducer = { state ->
                    val stroke = state.userStrokes[id] ?: return@Mutation state
                    state.copy(
                        userStrokes = state.userStrokes + (id to stroke.copy(points = points)),
                    )
                },
            ),
        )
    }

    fun hideUserStroke(id: Int, durationMillis: Long): List<Mutation> {
        return listOf(
            Mutation(
                scope = "userStrokes.$id.opacity",
                durationMillis = durationMillis,
                reducer = { state ->
                    val stroke = state.userStrokes[id] ?: return@Mutation state
                    state.copy(
                        userStrokes = state.userStrokes + (id to stroke.copy(opacity = 0f)),
                    )
                },
            ),
        )
    }

    fun removeAllUserStrokes(ids: List<Int>): List<Mutation> {
        if (ids.isEmpty()) return emptyList()
        return ids.map { id ->
            Mutation(
                scope = "userStrokes.$id",
                force = true,
                reducer = { state ->
                    state.copy(userStrokes = state.userStrokes - id)
                },
            )
        }
    }

    fun highlightCompleteChar(
        character: CharacterDefinition,
        color: ColorRgba?,
        durationMillis: Long,
    ): List<Mutation> {
        return CharacterActions.updateColor("highlightColor", color, 0) +
            CharacterActions.hideCharacter(CharacterLayer.HIGHLIGHT, character) +
            CharacterActions.showCharacter(CharacterLayer.HIGHLIGHT, character, durationMillis / 2) +
            CharacterActions.hideCharacter(CharacterLayer.HIGHLIGHT, character, durationMillis / 2)
    }

    val highlightStroke = CharacterActions::highlightStroke
}

private fun StrokeRenderState?.orDefault(index: Int): StrokeRenderState {
    return this ?: StrokeRenderState(strokeIndex = index)
}

private fun RenderStateSnapshot.updateLayer(
    layer: CharacterLayer,
    transform: (CharacterRenderState) -> CharacterRenderState,
): RenderStateSnapshot {
    return copy(character = character.updateLayer(layer, transform))
}

private fun CharacterLayersState.updateLayer(
    layer: CharacterLayer,
    transform: (CharacterRenderState) -> CharacterRenderState,
): CharacterLayersState {
    return when (layer) {
        CharacterLayer.MAIN -> copy(main = transform(main))
        CharacterLayer.OUTLINE -> copy(outline = transform(outline))
        CharacterLayer.HIGHLIGHT -> copy(highlight = transform(highlight))
    }
}
