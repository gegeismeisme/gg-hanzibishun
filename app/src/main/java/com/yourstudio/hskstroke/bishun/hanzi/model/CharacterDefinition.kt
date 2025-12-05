package com.yourstudio.hskstroke.bishun.hanzi.model

data class CharacterDefinition(
    val symbol: String,
    val strokes: List<Stroke>,
) {
    val strokeCount: Int = strokes.size
}
