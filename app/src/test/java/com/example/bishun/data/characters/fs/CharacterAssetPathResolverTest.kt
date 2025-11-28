package com.example.bishun.data.characters.fs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterAssetPathResolverTest {

    private val resolver = CharacterAssetPathResolver()

    @Test
    fun `returns code point and literal candidates`() {
        val candidates = resolver.assetCandidatesFor("永")

        assertEquals(
            listOf("characters/u6c38.json", "characters/永.json"),
            candidates,
        )
    }

    @Test
    fun `returns empty for blank input`() {
        val candidates = resolver.assetCandidatesFor("   ")
        assertTrue(candidates.isEmpty())
    }
}
