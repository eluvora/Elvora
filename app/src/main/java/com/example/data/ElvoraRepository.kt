package com.example.data

import com.example.data.local.ActivityLogEntity
import com.example.data.local.ElvoraDao
import com.example.data.local.FeedbackEntity
import com.example.data.local.LibraryItemEntity
import com.example.data.local.MistakeRecordEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.UserEntity
import com.example.data.prefs.ElvoraPreferences
import com.example.data.prefs.ThemePreference
import com.example.data.prefs.UserSettingsState
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

sealed class AuthResult {
    data class Success(val user: UserEntity, val isNewRegistration: Boolean) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class ElvoraRepository(
    private val dao: ElvoraDao,
    private val prefs: ElvoraPreferences
) {
    val settingsFlow: Flow<UserSettingsState> = prefs.settingsFlow

    fun observeLibrary(email: String): Flow<List<LibraryItemEntity>> = dao.observeLibraryItems(email)
    fun observeQuizAttempts(email: String): Flow<List<QuizAttemptEntity>> = dao.observeQuizAttempts(email)
    fun observeMistakes(email: String): Flow<List<MistakeRecordEntity>> = dao.observeMistakes(email)
    fun observeActivityLogs(email: String): Flow<List<ActivityLogEntity>> = dao.observeActivityLogs(email)
    fun observeFeedback(email: String): Flow<List<FeedbackEntity>> = dao.observeFeedback(email)

    suspend fun getUser(email: String): UserEntity? = dao.getUserByEmail(email.trim().lowercase())

    suspend fun registerLocalUser(name: String, email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        val existing = dao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return AuthResult.Error("An account with this email already exists on this device. Please sign in.")
        }
        val user = UserEntity(
            email = cleanEmail,
            name = cleanName,
            passwordHash = sha256(password),
            authMode = "LOCAL_DEMO"
        )
        dao.insertUser(user)
        prefs.setCurrentUserEmail(cleanEmail)
        prefs.setOnboardingCompleted(false)
        return AuthResult.Success(user, isNewRegistration = true)
    }

    suspend fun loginLocalUser(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val user = dao.getUserByEmail(cleanEmail)
            ?: return AuthResult.Error("No local account found for $cleanEmail. Register first or use Demo Student.")
        if (user.passwordHash != sha256(password)) {
            return AuthResult.Error("Incorrect password. Please check and try again.")
        }
        prefs.setCurrentUserEmail(cleanEmail)
        return AuthResult.Success(user, isNewRegistration = false)
    }

    suspend fun continueAsDemoStudent(): UserEntity {
        val demoEmail = "demo@elvora.local"
        val existing = dao.getUserByEmail(demoEmail)
        val user = existing ?: UserEntity(
            email = demoEmail,
            name = "ELVORA Learner",
            passwordHash = sha256("demo1234"),
            authMode = "LOCAL_DEMO"
        ).also { dao.insertUser(it) }
        prefs.setCurrentUserEmail(demoEmail)
        prefs.setOnboardingCompleted(false)
        return user
    }

    suspend fun resetLocalPassword(email: String, newPassword: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val user = dao.getUserByEmail(cleanEmail) ?: return false
        dao.updatePasswordHash(user.email, sha256(newPassword))
        return true
    }

    suspend fun updateUserName(email: String, name: String) {
        dao.updateUserName(email.trim().lowercase(), name.trim())
    }

    suspend fun logout() {
        prefs.setCurrentUserEmail("")
    }

    suspend fun completeOnboarding() {
        prefs.setOnboardingCompleted(true)
    }

    suspend fun setThemePreference(theme: ThemePreference) = prefs.setThemePreference(theme)
    suspend fun setMusicEnabled(enabled: Boolean) = prefs.setMusicEnabled(enabled)
    suspend fun setMusicVolume(volume: Float) = prefs.setMusicVolume(volume)
    suspend fun setReducedMotion(reduced: Boolean) = prefs.setReducedMotion(reduced)
    suspend fun setNotificationsEnabled(enabled: Boolean) = prefs.setNotificationsEnabled(enabled)
    suspend fun setFeedbackPromptDismissed(dismissed: Boolean) = prefs.setFeedbackPromptDismissed(dismissed)

    suspend fun saveToLibrary(
        userEmail: String,
        category: String,
        title: String,
        topic: String,
        contentJson: String,
        sourcePreview: String
    ): Long {
        val id = dao.insertLibraryItem(
            LibraryItemEntity(
                userEmail = userEmail,
                category = category,
                title = title,
                topic = topic,
                contentJson = contentJson,
                sourcePreview = sourcePreview.take(140)
            )
        )
        dao.insertActivityLog(
            ActivityLogEntity(
                userEmail = userEmail,
                featureName = "Saved to Library",
                topic = topic,
                detail = "$title ($category)"
            )
        )
        return id
    }

    suspend fun updateLibraryItem(item: LibraryItemEntity) {
        dao.updateLibraryItem(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteLibraryItem(id: Long) {
        dao.deleteLibraryItem(id)
    }

    suspend fun recordQuizAttempt(
        userEmail: String,
        featureType: String,
        topic: String,
        difficulty: String,
        score: Int,
        totalMarks: Int,
        correctCount: Int,
        totalQuestions: Int,
        diagnosisSummary: String,
        mistakes: List<MistakeRecordEntity>
    ): Long {
        val attemptId = dao.insertQuizAttempt(
            QuizAttemptEntity(
                userEmail = userEmail,
                featureType = featureType,
                topic = topic,
                difficulty = difficulty,
                score = score,
                totalMarks = totalMarks,
                correctCount = correctCount,
                totalQuestions = totalQuestions,
                diagnosisSummary = diagnosisSummary
            )
        )
        if (mistakes.isNotEmpty()) {
            dao.insertMistakes(mistakes.map { it.copy(attemptId = attemptId) })
        }
        dao.insertActivityLog(
            ActivityLogEntity(
                userEmail = userEmail,
                featureName = if (featureType == "QUIZ_FORGE") "QuizForge" else "Smart MCQs",
                topic = topic,
                detail = "Scored $score/$totalMarks ($difficulty)"
            )
        )
        return attemptId
    }

    suspend fun logActivity(
        userEmail: String,
        featureName: String,
        topic: String,
        detail: String,
        cardsLearntCount: Int = 0
    ) {
        dao.insertActivityLog(
            ActivityLogEntity(
                userEmail = userEmail,
                featureName = featureName,
                topic = topic,
                detail = detail,
                cardsLearntCount = cardsLearntCount
            )
        )
    }

    suspend fun submitFeedback(
        userEmail: String,
        feature: String,
        rating: Int,
        liked: String,
        improvements: String
    ): Long {
        return dao.insertFeedback(
            FeedbackEntity(
                userEmail = userEmail,
                feature = feature,
                rating = rating,
                liked = liked.trim(),
                improvements = improvements.trim()
            )
        )
    }

    suspend fun clearAllUserData(email: String) {
        dao.clearUserLibrary(email)
        dao.clearUserAttempts(email)
        dao.clearUserMistakes(email)
        dao.clearUserActivity(email)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
