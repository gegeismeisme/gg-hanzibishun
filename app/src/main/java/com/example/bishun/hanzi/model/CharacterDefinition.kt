package com.example.bishun.hanzi.model

data class CharacterDefinition(
    val symbol: String,
    val strokes: List<Stroke>,
) {
    val strokeCount: Int = strokes.size
}
