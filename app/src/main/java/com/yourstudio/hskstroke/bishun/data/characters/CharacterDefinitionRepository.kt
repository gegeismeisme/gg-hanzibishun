package com.yourstudio.hskstroke.bishun.data.characters

import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition

interface CharacterDefinitionRepository {
    suspend fun load(character: String): Result<CharacterDefinition>
}
