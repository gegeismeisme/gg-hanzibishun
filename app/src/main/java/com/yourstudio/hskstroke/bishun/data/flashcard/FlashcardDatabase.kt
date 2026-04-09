package com.yourstudio.hskstroke.bishun.data.flashcard

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FlashcardEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FlashcardDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
}

object FlashcardDatabaseProvider {
    @Volatile
    private var instance: FlashcardDatabase? = null

    fun get(context: Context): FlashcardDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                FlashcardDatabase::class.java,
                "flashcard.db",
            ).build().also { instance = it }
        }
    }
}
