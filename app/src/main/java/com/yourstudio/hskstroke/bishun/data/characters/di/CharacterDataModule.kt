package com.yourstudio.hskstroke.bishun.data.characters.di

import android.content.Context
import com.yourstudio.hskstroke.bishun.data.characters.CharacterDataRepository
import com.yourstudio.hskstroke.bishun.data.characters.CharacterDefinitionRepository
import com.yourstudio.hskstroke.bishun.data.characters.DefaultCharacterDataRepository
import com.yourstudio.hskstroke.bishun.data.characters.DefaultCharacterDefinitionRepository
import com.yourstudio.hskstroke.bishun.data.characters.cache.CharacterDiskCache
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterAssetDataSource
import com.yourstudio.hskstroke.bishun.data.characters.source.JsDelivrCharacterDataSource
import com.yourstudio.hskstroke.bishun.data.characters.source.OssCharacterDataSource
import com.yourstudio.hskstroke.bishun.hanzi.parser.CharacterParser
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.File

object CharacterDataModule {

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    fun provideDataRepository(context: Context): CharacterDataRepository {
        val assetDataSource = CharacterAssetDataSource(context.assets, json)
        val cacheDir = File(context.filesDir, "character_cache")
        val diskCache = CharacterDiskCache(cacheDir, json)
        val remoteSources = listOf(
            JsDelivrCharacterDataSource(httpClient, json),
            OssCharacterDataSource(httpClient, json),
        )
        return DefaultCharacterDataRepository(assetDataSource, diskCache, remoteSources)
    }

    fun provideDefinitionRepository(context: Context): CharacterDefinitionRepository {
        val dataRepository = provideDataRepository(context)
        return DefaultCharacterDefinitionRepository(dataRepository, CharacterParser())
    }

    @Deprecated("Use provideDataRepository for the raw JSON access instead.")
    fun provideRepository(context: Context): CharacterDataRepository =
        provideDataRepository(context)
}
