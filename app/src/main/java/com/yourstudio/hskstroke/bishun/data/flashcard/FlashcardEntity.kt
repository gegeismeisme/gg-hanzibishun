package com.yourstudio.hskstroke.bishun.data.flashcard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: Int,
    val hskLevel: Int,
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val example: String,
    val nextReviewAtEpochDay: Long,
    val intervalDays: Int,
    val easeFactor: Double,
    val repetition: Int,
    val lastQuality: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
