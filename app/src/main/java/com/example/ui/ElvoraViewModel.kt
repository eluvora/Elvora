package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientSynthPlayer
import com.example.audio.TtsLine
import com.example.audio.TtsSpeakerManager
import com.example.data.AuthResult
import com.example.data.ElvoraRepository
import com.example.data.local.ActivityLogEntity
import com.example.data.local.ElvoraDatabase
import com.example.data.local.FeedbackEntity
import com.example.data.local.LibraryItemEntity
import com.example.data.local.MistakeRecordEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.UserEntity
import com.example.data.prefs.ElvoraPreferences
import com.example.data.prefs.ThemePreference
import com.example.data.prefs.UserSettingsState
import com.example.intelligence.DiagnosisReport
import com.example.intelligence.DifficultyLevel
import com.example.intelligence.ElvoraIntelligenceEngine
import com.example.intelligence.FlipCardItem
import com.example.intelligence.McqQuestion
import com.example.intelligence.QuickAnswerResult
import com.example.intelligence.SmartSummaryResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ElvoraViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ElvoraDatabase.getInstance(application)
    private val prefs = ElvoraPreferences(application)
    private val repository = ElvoraRepository(db.elvoraDao(), prefs)

    val ambientPlayer = AmbientSynthPlayer()
    val ttsManager = TtsSpeakerManager(application)

    val settings: StateFlow<UserSettingsState> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsState())

    val currentUser: StateFlow<UserEntity?> = settings
        .map { it.currentUserEmail }
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(null) else flowOf(repository.getUser(email))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val libraryItems: StateFlow<List<LibraryItemEntity>> = settings
        .map { it.currentUserEmail }
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList()) else repository.observeLibrary(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = settings
        .map { it.currentUserEmail }
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList()) else repository.observeQuizAttempts(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<MistakeRecordEntity>> = settings
        .map { it.currentUserEmail }
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList()) else repository.observeMistakes(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogEntity>> = settings
        .map { it.currentUserEmail }
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList()) else repository.observeActivityLogs(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedbackList: StateFlow<List<FeedbackEntity>> = settings
        .map { it.currentUserEmail }
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList()) else repository.observeFeedback(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    val ttsReady: StateFlow<Boolean> = ttsManager.isReady
    val ttsSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val ttsActiveLine: StateFlow<Int> = ttsManager.activeLineIndex

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.setMusicEnabled(false)
            settings.collect { s ->
                ambientPlayer.setMuted(!s.musicEnabled)
                ambientPlayer.setVolume(s.musicVolume)
                if (!s.musicEnabled) {
                    ambientPlayer.stop()
                }
            }
        }
    }

    fun startAmbientMusicIfEnabled() {
        if (!settings.value.musicEnabled) return
        ambientPlayer.setMuted(false)
        ambientPlayer.setVolume(settings.value.musicVolume)
        ambientPlayer.start()
    }

    fun toggleMusic() {
        viewModelScope.launch {
            val next = !settings.value.musicEnabled
            repository.setMusicEnabled(next)
            if (next) {
                ambientPlayer.setMuted(false)
                ambientPlayer.setVolume(settings.value.musicVolume)
                ambientPlayer.start()
            } else {
                ambientPlayer.setMuted(true)
                ambientPlayer.stop()
            }
        }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch {
            repository.setMusicVolume(volume)
        }
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun clearAuthMessages() {
        _authUiState.value = AuthUiState()
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            when (val res = repository.registerLocalUser(name, email, password)) {
                is AuthResult.Success -> {
                    _authUiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authUiState.value = AuthUiState(isLoading = false, errorMessage = res.message)
                }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            when (val res = repository.loginLocalUser(email, password)) {
                is AuthResult.Success -> {
                    _authUiState.value = AuthUiState(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authUiState.value = AuthUiState(isLoading = false, errorMessage = res.message)
                }
            }
        }
    }

    fun continueAsDemoStudent(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            repository.continueAsDemoStudent()
            _authUiState.value = AuthUiState(isLoading = false)
            onSuccess()
        }
    }

    fun resetLocalPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            val ok = repository.resetLocalPassword(email, newPassword)
            _authUiState.value = if (ok) {
                AuthUiState(infoMessage = "Local demo password updated for $email. You can now sign in.")
            } else {
                AuthUiState(errorMessage = "No local account found for $email.")
            }
        }
    }

    fun completeWelcome() {
        viewModelScope.launch {
            ttsManager.stop()
            repository.completeOnboarding()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            ttsManager.stop()
            repository.logout()
            onLoggedOut()
        }
    }

    fun updateProfileName(newName: String) {
        val email = settings.value.currentUserEmail
        if (email.isBlank() || newName.isBlank()) return
        viewModelScope.launch {
            repository.updateUserName(email, newName)
            showMessage("Profile name updated.")
        }
    }

    fun setThemePreference(pref: ThemePreference) {
        viewModelScope.launch { repository.setThemePreference(pref) }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch { repository.setReducedMotion(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun dismissFeedbackPrompt() {
        viewModelScope.launch { repository.setFeedbackPromptDismissed(true) }
    }

    // Free Tools Persistence
    fun recordMcqQuizResult(
        topic: String,
        difficulty: DifficultyLevel,
        questions: List<McqQuestion>,
        selectedAnswers: Map<String, Int>,
        sourceMaterial: String
    ): DiagnosisReport {
        val report = ElvoraIntelligenceEngine.diagnoseQuizPerformance(
            questions = questions,
            selectedAnswers = selectedAnswers,
            previousMistakes = mistakes.value,
            sourceMaterial = sourceMaterial
        )
        val correctCount = questions.count { selectedAnswers[it.id] == it.correctIndex }
        val email = settings.value.currentUserEmail
        if (email.isNotBlank()) {
            val mistakeEntities = questions.mapNotNull { q ->
                val picked = selectedAnswers[q.id]
                if (picked != null && picked != q.correctIndex) {
                    val cat = report.mistakeBreakdown.keys.firstOrNull()?.label ?: "Concept misunderstanding"
                    MistakeRecordEntity(
                        userEmail = email,
                        attemptId = 0,
                        topic = q.topic,
                        questionText = q.question,
                        userAnswer = q.options.getOrElse(picked) { "" },
                        correctAnswer = q.options.getOrElse(q.correctIndex) { "" },
                        mistakeCategory = cat
                    )
                } else null
            }
            viewModelScope.launch {
                repository.recordQuizAttempt(
                    userEmail = email,
                    featureType = "SMART_MCQ",
                    topic = topic,
                    difficulty = difficulty.label,
                    score = correctCount,
                    totalMarks = questions.size,
                    correctCount = correctCount,
                    totalQuestions = questions.size,
                    diagnosisSummary = report.summaryMessage,
                    mistakes = mistakeEntities
                )
            }
        }
        return report
    }

    fun saveMcqSetToLibrary(topic: String, questions: List<McqQuestion>, sourceMaterial: String) {
        val email = settings.value.currentUserEmail
        if (email.isBlank() || questions.isEmpty()) return
        val arr = JSONArray()
        questions.forEach { q ->
            arr.put(JSONObject().apply {
                put("q", q.question)
                put("answer", q.options.getOrNull(q.correctIndex) ?: "")
                put("explanation", q.explanation)
            })
        }
        viewModelScope.launch {
            repository.saveToLibrary(
                userEmail = email,
                category = "MCQS",
                title = "Smart MCQs: $topic (${questions.size} Qs)",
                topic = topic,
                contentJson = arr.toString(),
                sourcePreview = sourceMaterial
            )
            showMessage("Saved Smart MCQs to your local Library.")
        }
    }

    fun markFlipCardsLearnt(topic: String, learntCount: Int) {
        val email = settings.value.currentUserEmail
        if (email.isBlank() || learntCount <= 0) return
        viewModelScope.launch {
            repository.logActivity(
                userEmail = email,
                featureName = "FlipCards",
                topic = topic,
                detail = "Marked $learntCount cards as learnt",
                cardsLearntCount = learntCount
            )
        }
    }

    fun saveFlipCardsToLibrary(topic: String, cards: List<FlipCardItem>, sourceMaterial: String) {
        val email = settings.value.currentUserEmail
        if (email.isBlank() || cards.isEmpty()) return
        val arr = JSONArray()
        cards.forEach { c ->
            arr.put(JSONObject().apply {
                put("front", c.front)
                put("back", c.back)
                put("learnt", c.isLearnt)
            })
        }
        viewModelScope.launch {
            repository.saveToLibrary(
                userEmail = email,
                category = "FLIPCARDS",
                title = "FlipCards: $topic (${cards.size} cards)",
                topic = topic,
                contentJson = arr.toString(),
                sourcePreview = sourceMaterial
            )
            showMessage("Saved FlipCards deck to your local Library.")
        }
    }

    fun saveSummaryToLibrary(summary: SmartSummaryResult, sourceMaterial: String) {
        val email = settings.value.currentUserEmail
        if (email.isBlank()) return
        val obj = JSONObject().apply {
            put("overview", summary.overview)
            put("bullets", JSONArray(summary.bulletPoints))
            put("keywords", JSONArray(summary.keywords))
            put("formulas", JSONArray(summary.formulas))
        }
        viewModelScope.launch {
            repository.saveToLibrary(
                userEmail = email,
                category = "SMART_SUMMARY",
                title = "Summary: ${summary.topic}",
                topic = summary.topic,
                contentJson = obj.toString(),
                sourcePreview = sourceMaterial
            )
            repository.logActivity(email, "Smart Summary", summary.topic, "Generated structured summary")
            showMessage("Saved Smart Summary to your local Library.")
        }
    }

    fun saveQuickAnswerToLibrary(result: QuickAnswerResult, topic: String) {
        val email = settings.value.currentUserEmail
        if (email.isBlank()) return
        val obj = JSONObject().apply {
            put("question", result.question)
            put("answer", result.conciseAnswer)
            put("explanation", result.simpleExplanation)
        }
        viewModelScope.launch {
            repository.saveToLibrary(
                userEmail = email,
                category = "QUICK_ANSWERS",
                title = "Q: ${result.question.take(45)}",
                topic = topic,
                contentJson = obj.toString(),
                sourcePreview = result.conciseAnswer
            )
            repository.logActivity(email, "Quick Answers", topic, "Answered: ${result.question.take(40)}")
            showMessage("Saved Quick Answer to your local Library.")
        }
    }

    fun updateLibraryItem(item: LibraryItemEntity) {
        viewModelScope.launch {
            repository.updateLibraryItem(item)
            showMessage("Library item updated.")
        }
    }

    fun deleteLibraryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteLibraryItem(id)
            showMessage("Removed from Library.")
        }
    }

    fun submitFeedback(feature: String, rating: Int, liked: String, improvements: String, onSaved: () -> Unit) {
        val email = settings.value.currentUserEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            val id = repository.submitFeedback(email, feature, rating, liked, improvements)
            if (id > 0) {
                showMessage("Feedback saved locally (Entry #$id). Thank you!")
                onSaved()
            }
        }
    }

    fun clearLocalUserData() {
        val email = settings.value.currentUserEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            repository.clearAllUserData(email)
            showMessage("Local learning records cleared.")
        }
    }

    fun speakText(text: String) = ttsManager.speakSingle(text)
    fun playTtsDialogue(lines: List<TtsLine>, rate: Float) = ttsManager.playSequence(lines, 0, rate)
    fun pauseTts() = ttsManager.pause()
    fun resumeTts() = ttsManager.resume()
    fun stopTts() = ttsManager.stop()
    fun setTtsRate(rate: Float) = ttsManager.setRate(rate)

    override fun onCleared() {
        super.onCleared()
        ambientPlayer.stop()
        ttsManager.shutdown()
    }
}
