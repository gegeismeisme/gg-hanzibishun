package com.yourstudio.hskstroke.bishun.data.characters

import com.yourstudio.hskstroke.bishun.data.characters.cache.CharacterDiskCache
import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterAssetDataSource

class DefaultCharacterDataRepository(
    private val assetDataSource: CharacterAssetDataSource,
    private val diskCache: CharacterDiskCache,
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

        return Result.failure(
            localResult.exceptionOrNull() ?: IllegalStateException("Unable to load $normalized"),
        )
    }
}
