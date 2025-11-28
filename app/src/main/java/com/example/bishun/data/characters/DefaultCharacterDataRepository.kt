package com.example.bishun.data.characters

import com.example.bishun.data.characters.model.CharacterJsonDto
import com.example.bishun.data.characters.source.CharacterAssetDataSource
import com.example.bishun.data.characters.source.JsDelivrCharacterDataSource

class DefaultCharacterDataRepository(
    private val assetDataSource: CharacterAssetDataSource,
    private val remoteDataSource: JsDelivrCharacterDataSource,
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

        val remoteResult = remoteDataSource.fetch(normalized)
        if (remoteResult.isSuccess) {
            return remoteResult
        }

        val combinedError = remoteResult.exceptionOrNull() ?: localResult.exceptionOrNull()
        return Result.failure(
            combinedError ?: IllegalStateException("Unable to load $normalized"),
        )
    }
}
