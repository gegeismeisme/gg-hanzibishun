package com.yourstudio.hskstroke.bishun.data.characters.source

import android.content.res.AssetManager
import com.yourstudio.hskstroke.bishun.data.characters.fs.CharacterAssetPathResolver
import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class CharacterAssetDataSource(
    private val assets: AssetManager,
    private val json: Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val pathResolver: CharacterAssetPathResolver = CharacterAssetPathResolver(),
) {

    suspend fun load(character: String): Result<CharacterJsonDto> = withContext(dispatcher) {
        val candidates = pathResolver.assetCandidatesFor(character)
        if (candidates.isEmpty()) {
            return@withContext Result.failure<CharacterJsonDto>(
                IllegalArgumentException("Character cannot be blank."),
            )
        }

        var lastError: Throwable? = null
        for (candidate in candidates) {
            val result = runCatching { readAsset(candidate) }
            if (result.isSuccess) {
                return@withContext result
            }
            lastError = result.exceptionOrNull()
        }

        return@withContext Result.failure(
            lastError ?: IOException("No assets found for $character"),
        )
    }

    private fun readAsset(path: String): CharacterJsonDto {
        assets.open(path).use { stream ->
            val buffer = stream.readBytes()
            return json.decodeFromString(CharacterJsonDto.serializer(), buffer.decodeToString())
        }
    }
}
