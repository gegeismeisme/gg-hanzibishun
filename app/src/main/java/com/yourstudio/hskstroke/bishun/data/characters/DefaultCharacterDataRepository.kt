package com.yourstudio.hskstroke.bishun.data.characters

import com.yourstudio.hskstroke.bishun.data.characters.cache.CharacterDiskCache
import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterAssetDataSource
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterPackedZipDataSource

class DefaultCharacterDataRepository(
    private val assetDataSource: CharacterAssetDataSource,
    private val diskCache: CharacterDiskCache,
    private val packedDataSource: CharacterPackedZipDataSource? = null,
) : CharacterDataRepository {

    override suspend fun loadCharacter(character: String): Result<CharacterJsonDto> {
        val normalized = character.trim()
        if (normalized.isEmpty()) {
            return Result.failure(IllegalArgumentException("Character cannot be blank."))
        }

        packedDataSource?.load(normalized)?.let { packedResult ->
            if (packedResult.isSuccess) {
                return packedResult
            }
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
