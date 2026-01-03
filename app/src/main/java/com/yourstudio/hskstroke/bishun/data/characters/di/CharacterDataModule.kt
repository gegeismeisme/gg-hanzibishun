package com.yourstudio.hskstroke.bishun.data.characters.di

import android.content.Context
import com.yourstudio.hskstroke.bishun.data.characters.CharacterDataRepository
import com.yourstudio.hskstroke.bishun.data.characters.CharacterDefinitionRepository
import com.yourstudio.hskstroke.bishun.data.characters.DefaultCharacterDataRepository
import com.yourstudio.hskstroke.bishun.data.characters.DefaultCharacterDefinitionRepository
import com.yourstudio.hskstroke.bishun.data.characters.cache.CharacterDiskCache
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterAssetDataSource
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterPackedZipDataSource
import com.yourstudio.hskstroke.bishun.hanzi.parser.CharacterParser
import kotlinx.serialization.json.Json
import java.io.File

object CharacterDataModule {

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    fun provideDataRepository(context: Context): CharacterDataRepository {
        val assetDataSource = CharacterAssetDataSource(context.assets, json)
        val packedDataSource = CharacterPackedZipDataSource(context.applicationContext, json)
        val cacheDir = File(context.filesDir, "character_cache")
        val diskCache = CharacterDiskCache(cacheDir, json)
        return DefaultCharacterDataRepository(assetDataSource, diskCache, packedDataSource)
    }

    fun provideDefinitionRepository(context: Context): CharacterDefinitionRepository {
        val dataRepository = provideDataRepository(context)
        return DefaultCharacterDefinitionRepository(dataRepository, CharacterParser())
    }

    @Deprecated("Use provideDataRepository for the raw JSON access instead.")
    fun provideRepository(context: Context): CharacterDataRepository =
        provideDataRepository(context)
}
