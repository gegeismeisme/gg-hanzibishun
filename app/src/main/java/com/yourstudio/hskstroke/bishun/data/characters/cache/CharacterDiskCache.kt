package com.yourstudio.hskstroke.bishun.data.characters.cache

import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class CharacterDiskCache(
    private val rootDir: File,
    private val json: Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    init {
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
    }

    suspend fun read(character: String): CharacterJsonDto? = withContext(dispatcher) {
        val file = fileFor(character)
        if (!file.exists()) return@withContext null
        runCatching {
            file.inputStream().use { stream ->
                json.decodeFromString(
                    CharacterJsonDto.serializer(),
                    stream.reader(Charsets.UTF_8).readText(),
                )
            }
        }.getOrNull()
    }

    suspend fun write(character: String, dto: CharacterJsonDto) = withContext(dispatcher) {
        val file = fileFor(character)
        val payload = json.encodeToString(CharacterJsonDto.serializer(), dto)
        runCatching {
            file.outputStream().use { stream ->
                stream.writer(Charsets.UTF_8).use { writer ->
                    writer.write(payload)
                }
            }
        }.onFailure { throwable ->
            throw IOException("Unable to cache character $character", throwable)
        }
    }

    private fun fileFor(character: String): File {
        val codePoint = Character.codePointAt(character, 0)
        val hex = codePoint.toString(16)
        return File(rootDir, "u$hex.json")
    }
}
