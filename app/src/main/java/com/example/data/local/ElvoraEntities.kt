package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val authMode: String = "LOCAL_DEMO"
)

@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val category: String, // MCQS, FLIPCARDS, SMART_SUMMARY, QUICK_ANSWERS, SMART_NOTES, CUSTOM_QUIZZES, REVISION_SHEETS, STUDYCAST
    val title: String,
    val topic: String,
    val contentJson: String,
    val sourcePreview: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val featureType: String, // SMART_MCQ or QUIZ_FORGE
    val topic: String,
    val difficulty: String,
    val score: Int,
    val totalMarks: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val diagnosisSummary: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mistake_records")
data class MistakeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val attemptId: Long,
    val topic: String,
    val questionText: String,
    val userAnswer: String,
    val correctAnswer: String,
    val mistakeCategory: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val featureName: String,
    val topic: String,
    val detail: String,
    val cardsLearntCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "feedback_submissions")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val feature: String,
    val rating: Int,
    val liked: String,
    val improvements: String,
    val createdAt: Long = System.currentTimeMillis()
)
