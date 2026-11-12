package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        LibraryItemEntity::class,
        QuizAttemptEntity::class,
        MistakeRecordEntity::class,
        ActivityLogEntity::class,
        FeedbackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ElvoraDatabase : RoomDatabase() {
    abstract fun elvoraDao(): ElvoraDao

    companion object {
        @Volatile
        private var INSTANCE: ElvoraDatabase? = null

        fun getInstance(context: Context): ElvoraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ElvoraDatabase::class.java,
                    "elvora_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
