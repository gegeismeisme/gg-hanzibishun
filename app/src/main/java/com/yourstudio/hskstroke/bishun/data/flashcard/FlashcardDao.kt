package com.yourstudio.hskstroke.bishun.data.flashcard

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<FlashcardEntity>)

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun countAll(): Int

    @Query("SELECT id FROM flashcards")
    suspend fun getAllIds(): List<Int>

    // --- Due words (nextReviewAtEpochDay <= today) ---
    @Query("""
        SELECT COUNT(*) FROM flashcards
        WHERE nextReviewAtEpochDay <= :todayEpochDay
          AND (:hskLevel = 0 OR hskLevel = :hskLevel)
    """)
    suspend fun countDue(todayEpochDay: Long, hskLevel: Int = 0): Int

    @Query("""
        SELECT * FROM flashcards
        WHERE nextReviewAtEpochDay <= :todayEpochDay
          AND (:hskLevel = 0 OR hskLevel = :hskLevel)
        ORDER BY nextReviewAtEpochDay ASC, id ASC
        LIMIT 1
    """)
    suspend fun getNextDue(todayEpochDay: Long, hskLevel: Int = 0): FlashcardEntity?

    // --- Weak words (lastQuality <= 3 OR repetition <= 1) ---
    @Query("""
        SELECT COUNT(*) FROM flashcards
        WHERE (lastQuality <= 3 OR repetition <= 1)
          AND nextReviewAtEpochDay <= :todayEpochDay
          AND (:hskLevel = 0 OR hskLevel = :hskLevel)
    """)
    suspend fun countWeak(todayEpochDay: Long, hskLevel: Int = 0): Int

    @Query("""
        SELECT * FROM flashcards
        WHERE (lastQuality <= 3 OR repetition <= 1)
          AND nextReviewAtEpochDay <= :todayEpochDay
          AND (:hskLevel = 0 OR hskLevel = :hskLevel)
        ORDER BY lastQuality ASC, nextReviewAtEpochDay ASC, id ASC
        LIMIT 1
    """)
    suspend fun getNextWeak(todayEpochDay: Long, hskLevel: Int = 0): FlashcardEntity?

    // --- Fallback (earliest scheduled) ---
    @Query("""
        SELECT * FROM flashcards
        WHERE (:hskLevel = 0 OR hskLevel = :hskLevel)
        ORDER BY nextReviewAtEpochDay ASC, id ASC
        LIMIT 1
    """)
    suspend fun getFallback(hskLevel: Int = 0): FlashcardEntity?

    // --- Stats ---
    @Query("SELECT COUNT(*) FROM flashcards WHERE hskLevel = :hskLevel")
    suspend fun countForLevel(hskLevel: Int): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE hskLevel <= :maxLevel")
    suspend fun countUpToLevel(maxLevel: Int): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE lastQuality >= 4 AND repetition >= 2")
    suspend fun countMastered(): Int

    @Update
    suspend fun update(card: FlashcardEntity)

    @Query("DELETE FROM flashcards")
    suspend fun clearAll()
}
