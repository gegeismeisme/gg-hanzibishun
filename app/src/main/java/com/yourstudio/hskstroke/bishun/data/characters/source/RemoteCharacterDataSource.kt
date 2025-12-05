package com.yourstudio.hskstroke.bishun.data.characters.source

import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto

interface RemoteCharacterDataSource {
    suspend fun fetch(character: String): Result<CharacterJsonDto>
}
