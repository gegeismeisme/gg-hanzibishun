package com.example.bishun.data.characters.source

import com.example.bishun.data.characters.model.CharacterJsonDto
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Loads characters from the official CDN: https://cdn.jsdelivr.net/npm/hanzi-writer-data.
 */
class JsDelivrCharacterDataSource(
    private val client: OkHttpClient,
    private val json: Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val datasetVersion: String = DEFAULT_VERSION,
) : RemoteCharacterDataSource {

    override suspend fun fetch(character: String): Result<CharacterJsonDto> = withContext(dispatcher) {
        if (character.isBlank()) {
            return@withContext Result.failure<CharacterJsonDto>(
                IllegalArgumentException("Character cannot be blank."),
            )
        }

        val codePoint = Character.codePointAt(character, 0)
        val literal = String(Character.toChars(codePoint))
        val encodedChar = URLEncoder.encode(literal, StandardCharsets.UTF_8.toString())
        val url =
            "https://cdn.jsdelivr.net/npm/hanzi-writer-data@$datasetVersion/$encodedChar.json"

        val request = Request.Builder().url(url).get().build()
        val result = runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected ${response.code} for $url")
                }
                val body = response.body ?: throw IOException("Empty body for $url")
                json.decodeFromString(
                    CharacterJsonDto.serializer(),
                    body.string(),
                )
            }
        }

        return@withContext result
    }

    companion object {
        private const val DEFAULT_VERSION = "2.0.1"
    }
}
