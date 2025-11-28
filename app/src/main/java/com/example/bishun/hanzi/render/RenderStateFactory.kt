package com.example.bishun.hanzi.render

import com.example.bishun.hanzi.model.CharacterDefinition

object RenderStateFactory {

    fun create(character: CharacterDefinition, options: RenderStateOptions): RenderStateSnapshot {
        val optionState = options.toRenderOptionsState()
        val mainOpacity = if (options.showCharacter) 1f else 0f
        val outlineOpacity = if (options.showOutline) 1f else 0f

        val baseStrokes = character.strokes.associate { stroke ->
            stroke.strokeNum to StrokeRenderState(
                strokeIndex = stroke.strokeNum,
                opacity = mainOpacity,
                displayPortion = mainOpacity,
            )
        }

        val outlineStrokes = character.strokes.associate { stroke ->
            stroke.strokeNum to StrokeRenderState(
                strokeIndex = stroke.strokeNum,
                opacity = outlineOpacity,
                displayPortion = outlineOpacity,
            )
        }

        val highlightStrokes = character.strokes.associate { stroke ->
            stroke.strokeNum to StrokeRenderState(
                strokeIndex = stroke.strokeNum,
                opacity = 0f,
                displayPortion = 0f,
            )
        }

        return RenderStateSnapshot(
            options = optionState,
            character = CharacterLayersState(
                main = CharacterRenderState(mainOpacity, baseStrokes),
                outline = CharacterRenderState(outlineOpacity, outlineStrokes),
                highlight = CharacterRenderState(0f, highlightStrokes),
            ),
            quiz = QuizRenderState(),
        )
    }
}
