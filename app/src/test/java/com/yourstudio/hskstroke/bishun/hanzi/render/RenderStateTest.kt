package com.yourstudio.hskstroke.bishun.hanzi.render

import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.model.Stroke
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RenderStateTest {

    private val sampleCharacter = CharacterDefinition(
        symbol = "\u6c38",
        strokes = listOf(
            Stroke("M 0 0 L 10 0", listOf(Point(0.0, 0.0), Point(10.0, 0.0)), strokeNum = 0),
            Stroke("M 0 0 L 0 10", listOf(Point(0.0, 0.0), Point(0.0, 10.0)), strokeNum = 1),
        ),
    )

    @Test
    fun `outline visibility follows options`() {
        val state = RenderState(sampleCharacter, RenderStateOptions(showOutline = false))

        assertEquals(0f, state.state.value.character.outline.opacity)
        assertEquals(0f, state.state.value.character.outline.strokes[0]?.opacity)
    }

    @Test
    fun `mutation updates character opacity`() = runBlocking {
        val state = RenderState(sampleCharacter)
        val mutation = Mutation(
            scope = "character.main.opacity",
            reducer = { current ->
                current.copy(
                    character = current.character.copy(
                        main = current.character.main.copy(opacity = 0.25f),
                    ),
                )
            },
        )

        state.run(mutation)

        assertEquals(0.25f, state.state.value.character.main.opacity)
    }

    @Test
    fun `animated mutation reaches final state`() = runBlocking {
        val state = RenderState(sampleCharacter)
        val mutation = Mutation(
            scope = "character.highlight",
            reducer = { current ->
                current.copy(
                    character = current.character.copy(
                        highlight = current.character.highlight.copy(opacity = 1f),
                    ),
                )
            },
            durationMillis = 32,
        )

        state.run(mutation)

        assertEquals(1f, state.state.value.character.highlight.opacity)
    }
}
