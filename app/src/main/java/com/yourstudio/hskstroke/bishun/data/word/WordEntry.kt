package com.yourstudio.hskstroke.bishun.data.word

data class WordEntry(
    val word: String,
    val oldword: String,
    val strokes: String,
    val pinyin: String,
    val radicals: String,
    val explanation: String,
    val more: String,
)
