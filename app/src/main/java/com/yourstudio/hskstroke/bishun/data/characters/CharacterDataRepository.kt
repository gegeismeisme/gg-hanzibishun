package com.example.bishun.data.characters

import com.example.bishun.data.characters.model.CharacterJsonDto

/**
 * Loads raw Hanzi Writer JSON payloads from local assets or the CDN.
 * Stage 2 will map these DTOs onto domain models/geometry objects.
 */
interface CharacterDataRepository {
    /**
     * @param character Single Hanzi literal or code point surrogate pair.
     * @return [CharacterJsonDto] packaged in a [Result].
     */
    suspend fun loadCharacter(character: String): Result<CharacterJsonDto>
}
