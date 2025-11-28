package com.example.bishun.data.characters

import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.parser.CharacterParser

class DefaultCharacterDefinitionRepository(
    private val dataRepository: CharacterDataRepository,
    private val parser: CharacterParser,
) : CharacterDefinitionRepository {

    override suspend fun load(character: String): Result<CharacterDefinition> {
        return dataRepository.loadCharacter(character).mapCatching { dto ->
            parser.parse(character, dto)
        }
    }
}
