package com.yourstudio.hskstroke.bishun.data.flashcard

import java.time.LocalDate
import java.time.ZoneOffset

class FlashcardRepository(
    private val dao: FlashcardDao,
    private val seedLoader: FlashcardSeedLoader,
) {

    private val todayEpochDay: Long
        get() = LocalDate.now(ZoneOffset.UTC).toEpochDay()

    /**
     * Seeds the database from assets if empty or if seed IDs differ.
     * Call once on first Flashcard tab visit.
     */
    suspend fun seedIfNeeded() {
        val seedWords = seedLoader.load()
        val dbCount = dao.countAll()
        if (dbCount == 0) {
            val now = System.currentTimeMillis()
            val today = todayEpochDay
            dao.insertAll(seedWords.map { it.toEntity(today, now) })
            return
        }
        // Check for seed updates
        val seedIds = seedWords.map { it.id }.toSet()
        val dbIds = dao.getAllIds().toSet()
        if (seedIds != dbIds) {
            dao.clearAll()
            val now = System.currentTimeMillis()
            val today = todayEpochDay
            dao.insertAll(seedWords.map { it.toEntity(today, now) })
        }
    }

    /**
     * Get next card for review. Priority: due → weak → fallback.
     * @param isPro Whether user has Pro entitlement (unlocks HSK 2-7)
     * @param selectedLevel 0 = all levels, 1-7 = specific HSK level
     */
    suspend fun getNextCard(isPro: Boolean, selectedLevel: Int = 0): StudyCard? {
        val level = if (isPro) selectedLevel else 1 // Free = HSK 1 only
        val today = todayEpochDay

        val due = dao.getNextDue(today, level)
        if (due != null) return due.toStudyCard()

        val weak = dao.getNextWeak(today, level)
        if (weak != null) return weak.toStudyCard()

        return dao.getFallback(level)?.toStudyCard()
    }

    /**
     * Submit a review rating for the current card.
     * Runs SM-2 algorithm and persists the updated card.
     */
    suspend fun submitReview(card: StudyCard, rating: StudyRating): StudyCard {
        val today = todayEpochDay
        val result = Sm2Scheduler.calculate(
            currentIntervalDays = card.intervalDays,
            currentEaseFactor = card.easeFactor,
            currentRepetition = card.repetition,
            quality = rating.quality,
            todayEpochDay = today,
        )
        val now = System.currentTimeMillis()
        val updatedEntity = FlashcardEntity(
            id = card.id,
            hskLevel = card.hskLevel,
            hanzi = card.hanzi,
            pinyin = card.pinyin,
            english = card.english,
            example = card.example,
            nextReviewAtEpochDay = result.nextReviewAtEpochDay,
            intervalDays = result.intervalDays,
            easeFactor = result.easeFactor,
            repetition = result.repetition,
            lastQuality = rating.quality,
            createdAtEpochMillis = now, // preserved but not critical
            updatedAtEpochMillis = now,
        )
        dao.update(updatedEntity)
        return updatedEntity.toStudyCard()
    }

    /**
     * Get current review statistics.
     * @param isPro Whether user has Pro (affects level filter)
     * @param selectedLevel 0 = all, 1-7 = specific
     */
    suspend fun getStats(isPro: Boolean, selectedLevel: Int = 0): FlashcardStats {
        val level = if (isPro) selectedLevel else 1
        val today = todayEpochDay
        return FlashcardStats(
            dueCount = dao.countDue(today, level),
            weakCount = dao.countWeak(today, level),
            masteredCount = dao.countMastered(),
            totalCount = if (level == 0) dao.countAll() else dao.countForLevel(level),
        )
    }

    /**
     * Clear all flashcard data (for settings clear-data flow).
     */
    suspend fun clearAll() {
        dao.clearAll()
    }
}
