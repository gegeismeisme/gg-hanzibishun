package com.yourstudio.hskstroke.bishun.data.flashcard

data class SeedWord(
    val id: Int,
    val hskLevel: Int,
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val example: String,
)

data class StudyCard(
    val id: Int,
    val hskLevel: Int,
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val example: String,
    val intervalDays: Int,
    val easeFactor: Double,
    val repetition: Int,
    val lastQuality: Int,
    val nextReviewAtEpochDay: Long,
)

enum class StudyRating(val quality: Int) {
    Again(1),
    Hard(3),
    Good(4),
    Easy(5),
}

data class FlashcardStats(
    val dueCount: Int,
    val weakCount: Int,
    val masteredCount: Int,
    val totalCount: Int,
)

fun SeedWord.toEntity(todayEpochDay: Long, nowMillis: Long) = FlashcardEntity(
    id = id,
    hskLevel = hskLevel,
    hanzi = hanzi,
    pinyin = pinyin,
    english = english,
    example = example,
    nextReviewAtEpochDay = todayEpochDay,
    intervalDays = 0,
    easeFactor = 2.5,
    repetition = 0,
    lastQuality = 0,
    createdAtEpochMillis = nowMillis,
    updatedAtEpochMillis = nowMillis,
)

fun FlashcardEntity.toStudyCard() = StudyCard(
    id = id,
    hskLevel = hskLevel,
    hanzi = hanzi,
    pinyin = pinyin,
    english = english,
    example = example,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    repetition = repetition,
    lastQuality = lastQuality,
    nextReviewAtEpochDay = nextReviewAtEpochDay,
)
