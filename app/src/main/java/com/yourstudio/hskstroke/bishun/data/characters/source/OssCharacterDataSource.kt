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

class OssCharacterDataSource(
    private val client: OkHttpClient,
    private val json: Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val baseUrl: String = DEFAULT_BASE_URL,
) : RemoteCharacterDataSource {

    override suspend fun fetch(character: String): Result<CharacterJsonDto> = withContext(dispatcher) {
        if (character.isBlank()) {
            return@withContext Result.failure<CharacterJsonDto>(
                IllegalArgumentException("Character cannot be blank."),
            )
        }
        val literal = String(Character.toChars(Character.codePointAt(character, 0)))
        val encodedChar = URLEncoder.encode(literal, StandardCharsets.UTF_8)
        val request = Request.Builder()
            .url("$baseUrl/$encodedChar.json")
            .get()
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected ${response.code} for ${request.url}")
                val body = response.body ?: throw IOException("Empty body for ${request.url}")
                json.decodeFromString(CharacterJsonDto.serializer(), body.string())
            }
        }
    }

    companion object {
        private const val DEFAULT_BASE_URL = "https://hanzibishun.oss-cn-hangzhou.aliyuncs.com/package"
    }
}
