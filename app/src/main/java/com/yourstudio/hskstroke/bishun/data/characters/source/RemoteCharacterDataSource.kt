package com.example.bishun.data.characters.source

import com.example.bishun.data.characters.model.CharacterJsonDto

interface RemoteCharacterDataSource {
    suspend fun fetch(character: String): Result<CharacterJsonDto>
}
