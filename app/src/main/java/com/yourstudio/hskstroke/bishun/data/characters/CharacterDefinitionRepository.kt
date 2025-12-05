package com.example.bishun.data.characters

import com.example.bishun.hanzi.model.CharacterDefinition

interface CharacterDefinitionRepository {
    suspend fun load(character: String): Result<CharacterDefinition>
}
