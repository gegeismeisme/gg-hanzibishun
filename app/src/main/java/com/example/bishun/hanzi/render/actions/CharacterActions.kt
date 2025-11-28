package com.example.bishun.hanzi.render.actions

import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Stroke
import com.example.bishun.hanzi.render.CharacterLayersState
import com.example.bishun.hanzi.render.CharacterRenderState
import com.example.bishun.hanzi.render.ColorParser
import com.example.bishun.hanzi.render.ColorRgba
import com.example.bishun.hanzi.render.Mutation
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.hanzi.render.StrokeRenderState

enum class CharacterLayer(val scope: String) {
    MAIN("main"),
    OUTLINE("outline"),
    HIGHLIGHT("highlight"),
}

object CharacterActions {

    fun showStrokes(
        layer: CharacterLayer,
        character: CharacterDefinition,
        durationMillis: Long,
    ): List<Mutation> {
        return listOf(
            Mutation(
                scope = "character.${layer.scope}.strokes",
                durationMillis = durationMillis,
                force = true,
                reducer = { state ->
                    state.updateLayer(layer) {
                        it.copy(
                            strokes = character.strokes.associate { stroke ->
                                stroke.strokeNum to StrokeRenderState(
                                    strokeIndex = stroke.strokeNum,
                                    opacity = 1f,
                                    displayPortion = 1f,
                                )
                            },
                        )
                    }
                },
            ),
        )
    }

    fun showCharacter(
        layer: CharacterLayer,
        character: CharacterDefinition,
        durationMillis: Long,
    ): List<Mutation> {
        return listOf(
            Mutation(
                scope = "character.${layer.scope}",
                durationMillis = durationMillis,
                force = true,
                reducer = { state ->
                    state.updateLayer(layer) {
                        it.copy(
                            opacity = 1f,
                            strokes = character.strokes.associate { stroke ->
                                stroke.strokeNum to StrokeRenderState(
                                    strokeIndex = stroke.strokeNum,
                                    opacity = 1f,
                                    displayPortion = 1f,
                                )
                            },
                        )
                    }
                },
            ),
        )
    }

    fun hideCharacter(
        layer: CharacterLayer,
        character: CharacterDefinition,
        durationMillis: Long = 0,
    ): List<Mutation> {
        val hideOpacity = Mutation(
            scope = "character.${layer.scope}.opacity",
            durationMillis = durationMillis,
            force = true,
            reducer = { state ->
                state.updateLayer(layer) { it.copy(opacity = 0f) }
            },
        )
        return listOf(hideOpacity) + showStrokes(layer, character, 0)
    }

    fun updateColor(optionName: String, color: ColorRgba?, durationMillis: Long): List<Mutation> {
        return listOf(
            Mutation(
                scope = "options.$optionName",
                durationMillis = durationMillis,
                reducer = { state ->
                    val updatedOptions = when (optionName) {
                        "strokeColor" -> state.options.copy(
                            strokeColor = color ?: state.options.strokeColor,
                        )
                        "outlineColor" -> state.options.copy(
                            outlineColor = color ?: state.options.outlineColor,
                        )
                        "highlightColor" -> state.options.copy(
                            highlightColor = color ?: state.options.highlightColor,
                        )
                        "drawingColor" -> state.options.copy(
                            drawingColor = color ?: state.options.drawingColor,
                        )
                        else -> state.options
                    }
                    state.copy(options = updatedOptions)
                },
            ),
        )
    }

    fun highlightStroke(
        stroke: Stroke,
        color: ColorRgba?,
        speed: Double,
    ): List<Mutation> {
        val duration = ((stroke.length() + 600) / (3 * speed)).toLong()
        val targetColor = color ?: ColorParser.parse("#AAAAFF")
        val setColor = Mutation(
            scope = "options.highlightColor",
            reducer = { state ->
                state.copy(options = state.options.copy(highlightColor = targetColor))
            },
        )
        val primeStroke = Mutation(
            scope = "character.highlight",
            force = true,
            reducer = { state ->
                state.updateLayer(CharacterLayer.HIGHLIGHT) {
                    it.copy(
                        opacity = 1f,
                        strokes = mapOf(
                            stroke.strokeNum to StrokeRenderState(
                                strokeIndex = stroke.strokeNum,
                                displayPortion = 0f,
                                opacity = 0f,
                            ),
                        ),
                    )
                }
            },
        )
        val animateStroke = Mutation(
            scope = "character.highlight.strokes.${stroke.strokeNum}",
            durationMillis = duration,
            reducer = { state ->
                state.updateStroke(CharacterLayer.HIGHLIGHT, stroke.strokeNum) {
                    it.copy(displayPortion = 1f, opacity = 1f)
                }
            },
        )
        val fadeOut = Mutation(
            scope = "character.highlight.strokes.${stroke.strokeNum}.opacity",
            durationMillis = duration,
            force = true,
            reducer = { state ->
                state.updateStroke(CharacterLayer.HIGHLIGHT, stroke.strokeNum) {
                    it.copy(opacity = 0f)
                }
            },
        )
        return listOf(setColor, primeStroke, animateStroke, fadeOut)
    }

    fun showStroke(
        layer: CharacterLayer,
        strokeNum: Int,
        durationMillis: Long,
    ): List<Mutation> {
        return listOf(
            Mutation(
                scope = "character.${layer.scope}.strokes.$strokeNum",
                durationMillis = durationMillis,
                force = true,
                reducer = { state ->
                    state.updateStroke(layer, strokeNum) {
                        it.copy(displayPortion = 1f, opacity = 1f)
                    }
                },
            ),
        )
    }
}

private fun RenderStateSnapshot.updateLayer(
    layer: CharacterLayer,
    transform: (CharacterRenderState) -> CharacterRenderState,
): RenderStateSnapshot {
    return copy(character = character.updateLayer(layer, transform))
}

private fun RenderStateSnapshot.updateStroke(
    layer: CharacterLayer,
    strokeNum: Int,
    transform: (StrokeRenderState) -> StrokeRenderState,
): RenderStateSnapshot {
    return updateLayer(layer) { charState ->
        val current = charState.strokes[strokeNum] ?: StrokeRenderState(strokeIndex = strokeNum)
        charState.copy(strokes = charState.strokes + (strokeNum to transform(current)))
    }
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
