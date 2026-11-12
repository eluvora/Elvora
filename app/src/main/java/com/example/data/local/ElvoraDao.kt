package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ElvoraDao {
    // Users
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET name = :name WHERE email = :email")
    suspend fun updateUserName(email: String, name: String)

    @Query("UPDATE users SET passwordHash = :newHash WHERE email = :email")
    suspend fun updatePasswordHash(email: String, newHash: String)

    // Library
    @Query("SELECT * FROM library_items WHERE userEmail = :email ORDER BY updatedAt DESC")
    fun observeLibraryItems(email: String): Flow<List<LibraryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryItem(item: LibraryItemEntity): Long

    @Update
    suspend fun updateLibraryItem(item: LibraryItemEntity)

    @Query("DELETE FROM library_items WHERE id = :id")
    suspend fun deleteLibraryItem(id: Long)

    // Quiz Attempts & Mistakes
    @Query("SELECT * FROM quiz_attempts WHERE userEmail = :email ORDER BY createdAt DESC")
    fun observeQuizAttempts(email: String): Flow<List<QuizAttemptEntity>>

    @Insert
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT * FROM mistake_records WHERE userEmail = :email ORDER BY createdAt DESC")
    fun observeMistakes(email: String): Flow<List<MistakeRecordEntity>>

    @Insert
    suspend fun insertMistakes(mistakes: List<MistakeRecordEntity>)

    // Activity Logs
    @Query("SELECT * FROM activity_logs WHERE userEmail = :email ORDER BY createdAt DESC")
    fun observeActivityLogs(email: String): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertActivityLog(log: ActivityLogEntity)

    // Feedback
    @Query("SELECT * FROM feedback_submissions WHERE userEmail = :email ORDER BY createdAt DESC")
    fun observeFeedback(email: String): Flow<List<FeedbackEntity>>

    @Insert
    suspend fun insertFeedback(feedback: FeedbackEntity): Long

    // Clear user local data
    @Query("DELETE FROM library_items WHERE userEmail = :email")
    suspend fun clearUserLibrary(email: String)

    @Query("DELETE FROM quiz_attempts WHERE userEmail = :email")
    suspend fun clearUserAttempts(email: String)

    @Query("DELETE FROM mistake_records WHERE userEmail = :email")
    suspend fun clearUserMistakes(email: String)

    @Query("DELETE FROM activity_logs WHERE userEmail = :email")
    suspend fun clearUserActivity(email: String)
}
