package com.yourstudio.hskstroke.bishun.data.characters.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the structure exposed by https://github.com/chanind/hanzi-writer-data.
 * The DTO stays close to the raw JSON so it can be cached/serialized cheaply.
 */
@Serializable
data class CharacterJsonDto(
    val strokes: List<String>,
    val medians: List<List<List<Float>>>,
    @SerialName("radStrokes") val radicalStrokes: List<Int>? = null,
)
