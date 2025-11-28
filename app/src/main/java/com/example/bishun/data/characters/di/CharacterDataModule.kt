package com.example.bishun.data.characters.di

import android.content.Context
import com.example.bishun.data.characters.CharacterDataRepository
import com.example.bishun.data.characters.DefaultCharacterDataRepository
import com.example.bishun.data.characters.source.CharacterAssetDataSource
import com.example.bishun.data.characters.source.JsDelivrCharacterDataSource
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

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

    fun provideRepository(context: Context): CharacterDataRepository {
        val assetDataSource = CharacterAssetDataSource(context.assets, json)
        val remoteDataSource = JsDelivrCharacterDataSource(httpClient, json)
        return DefaultCharacterDataRepository(assetDataSource, remoteDataSource)
    }
}
