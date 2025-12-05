package com.yourstudio.hskstroke.bishun.data.characters

import com.yourstudio.hskstroke.bishun.data.characters.cache.CharacterDiskCache
import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterAssetDataSource
import com.yourstudio.hskstroke.bishun.data.characters.source.RemoteCharacterDataSource

class DefaultCharacterDataRepository(
    private val assetDataSource: CharacterAssetDataSource,
    private val diskCache: CharacterDiskCache,
    private val remoteSources: List<RemoteCharacterDataSource>,
) : CharacterDataRepository {

    override suspend fun loadCharacter(character: String): Result<CharacterJsonDto> {
        val normalized = character.trim()
        if (normalized.isEmpty()) {
            return Result.failure(IllegalArgumentException("Character cannot be blank."))
        }

        val localResult = assetDataSource.load(normalized)
        if (localResult.isSuccess) {
            return localResult
        }

        diskCache.read(normalized)?.let {
            return Result.success(it)
        }

        var lastError: Throwable? = localResult.exceptionOrNull()
        remoteSources.forEach { source ->
            val remoteResult = source.fetch(normalized)
            if (remoteResult.isSuccess) {
                val dto = remoteResult.getOrThrow()
                runCatching { diskCache.write(normalized, dto) }
                return remoteResult
            }
            lastError = remoteResult.exceptionOrNull() ?: lastError
        }

        return Result.failure(
            lastError ?: IllegalStateException("Unable to load $normalized"),
        )
    }
}
