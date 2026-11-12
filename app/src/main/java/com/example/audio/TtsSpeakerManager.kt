package com.example.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TtsLine(
    val id: String,
    val speaker: String,
    val text: String,
    val pitch: Float = 1.0f,
    val rateFactor: Float = 0.94f,
    val localeTag: String = "en-US"
)

class TtsSpeakerManager(context: Context) {
    private var tts: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeLineIndex = MutableStateFlow(-1)
    val activeLineIndex: StateFlow<Int> = _activeLineIndex.asStateFlow()

    private var queue: List<TtsLine> = emptyList()
    private var currentIndex: Int = 0
    private var isPaused: Boolean = false
    private var speechRate: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        if (!isPaused) {
                            currentIndex++
                            if (currentIndex < queue.size) {
                                mainHandler.postDelayed({
                                    if (!isPaused) speakCurrentLine()
                                }, 180L)
                            } else {
                                _isSpeaking.value = false
                                _activeLineIndex.value = -1
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
                _isReady.value = true
            }
        }
    }

    fun speakSingle(text: String, rate: Float = speechRate) {
        if (!_isReady.value || text.isBlank()) return
        queue = listOf(TtsLine("single", "Maya", text, pitch = 1.22f, rateFactor = 0.94f))
        currentIndex = 0
        isPaused = false
        speechRate = rate
        speakCurrentLine()
    }

    fun playSequence(lines: List<TtsLine>, startIndex: Int = 0, rate: Float = speechRate) {
        if (!_isReady.value || lines.isEmpty()) return
        mainHandler.removeCallbacksAndMessages(null)
        queue = lines
        currentIndex = startIndex.coerceIn(0, lines.lastIndex)
        isPaused = false
        speechRate = rate
        speakCurrentLine()
    }

    fun pause() {
        isPaused = true
        mainHandler.removeCallbacksAndMessages(null)
        _isSpeaking.value = false
        tts?.stop()
    }

    fun resume() {
        if (queue.isEmpty() || !_isReady.value) return
        if (currentIndex >= queue.size) currentIndex = 0
        isPaused = false
        speakCurrentLine()
    }

    fun setRate(rate: Float) {
        speechRate = rate
        if (_isSpeaking.value && !isPaused) {
            tts?.stop()
            speakCurrentLine()
        }
    }

    fun stop() {
        isPaused = false
        mainHandler.removeCallbacksAndMessages(null)
        queue = emptyList()
        currentIndex = 0
        _isSpeaking.value = false
        _activeLineIndex.value = -1
        tts?.stop()
    }

    private fun speakCurrentLine() {
        if (currentIndex !in queue.indices) return
        val engine = tts ?: return
        val line = queue[currentIndex]
        _activeLineIndex.value = currentIndex

        val targetLocale = if (line.localeTag.startsWith("hi")) Locale("hi", "IN") else Locale.US
        val langResult = engine.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.language = Locale.US
        }

        val isMaya = line.speaker == "Maya"
        selectNaturalVoice(engine, targetLocale, isSoftFemale = isMaya)

        // Soft, cute expressive lift for Maya in both English & Hindi; calm natural tone for Alex
        val questionLift = if (line.text.trimEnd().endsWith("?")) 0.04f else 0.0f
        val basePitch = if (isMaya) line.pitch.coerceAtLeast(1.20f) else line.pitch
        engine.setPitch((basePitch + questionLift).coerceIn(0.75f, 1.32f))
        engine.setSpeechRate((speechRate * line.rateFactor).coerceIn(0.45f, 2.0f))
        engine.speak(line.text, TextToSpeech.QUEUE_FLUSH, null, line.id)
    }

    private fun selectNaturalVoice(engine: TextToSpeech, locale: Locale, isSoftFemale: Boolean) {
        val voices = runCatching { engine.voices }.getOrNull()?.toList().orEmpty()
        if (voices.isEmpty()) return

        val langVoices = voices.filter {
            it.locale.language.equals(locale.language, ignoreCase = true) &&
                !it.isNetworkConnectionRequired
        }.ifEmpty {
            voices.filter { it.locale.language.equals(locale.language, ignoreCase = true) }
        }
        if (langVoices.isEmpty()) return

        val preferred = langVoices.firstOrNull { v ->
            matchesVoiceProfile(v, isSoftFemale = isSoftFemale)
        } ?: langVoices.first()

        runCatching { engine.voice = preferred }
    }

    private fun matchesVoiceProfile(voice: Voice, isSoftFemale: Boolean): Boolean {
        val name = voice.name.lowercase()
        val features = voice.features?.joinToString(",")?.lowercase().orEmpty()
        return if (isSoftFemale) {
            name.contains("female") || name.contains("woman") ||
                name.contains("sfg") || name.contains("tpf") || name.contains("iob") ||
                name.contains("hi-in-x-hie") || name.contains("hi-in-x-hia") ||
                name.contains("hi-in-x-hic") || name.contains("hi-in-x-cfn") ||
                name.contains("en-us-x-sfg") || name.contains("en-us-x-tpf") ||
                features.contains("female")
        } else {
            (name.contains("male") && !name.contains("female")) ||
                name.contains("iom") || name.contains("iol") || name.contains("tpd") ||
                name.contains("hi-in-x-hid") || name.contains("hi-in-x-hif") ||
                name.contains("en-us-x-iom") ||
                features.contains("male")
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
