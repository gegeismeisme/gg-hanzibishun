package com.yourstudio.hskstroke.bishun.data.characters.cache

import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CharacterDiskCacheTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun writesAndReadsCharacter() = runBlocking {
        val tempDir = Files.createTempDirectory("char-cache").toFile()
        val cache = CharacterDiskCache(tempDir, json)
        val dto = CharacterJsonDto(
            strokes = listOf("M 0 0 L 10 0"),
            medians = listOf(listOf(listOf(0f, 0f), listOf(10f, 0f))),
        )

        cache.write("\u6c38", dto)
        val restored = cache.read("\u6c38")

        assertNotNull(restored)
        val nonNullRestored = restored!!
        assertEquals(dto.strokes, nonNullRestored.strokes)
        assertEquals(dto.medians, nonNullRestored.medians)
    }
}
