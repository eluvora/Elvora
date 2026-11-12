package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.SoftSkyBlue
import com.example.ui.theme.DeepMidnightBlue
import com.example.ui.theme.DeepRosePink
import com.example.ui.theme.LocalElvoraTokens
import com.example.ui.theme.SoftPinkGlow
import com.example.ui.theme.VividMagentaPink
import kotlinx.coroutines.delay

private data class WelcomeSlide(
    val headline: String,
    val whyElvoraTag: String,
    val subtitleNote: String,
    val icon: ImageVector
)

private val welcomeSlides = listOf(
    WelcomeSlide(
        headline = "Finding learning difficult? Too much to study, but don't know where to start?",
        whyElvoraTag = "WHY ELVORA? • CLARITY OVER OVERLOAD",
        subtitleNote = "Step 1 of 7 — Turning bulky notes into clear study structure.",
        icon = Icons.Filled.MenuBook
    ),
    WelcomeSlide(
        headline = "No worries! ELVORA is here to make learning easier, smarter and more enjoyable.",
        whyElvoraTag = "WHY ELVORA? • DESIGNED FOR STUDENTS",
        subtitleNote = "Step 2 of 7 — Calm focus in blue & magenta academic aesthetic.",
        icon = Icons.Filled.AutoAwesome
    ),
    WelcomeSlide(
        headline = "Introducing the ELVORA Method — turn your study material into effective learning experiences.",
        whyElvoraTag = "THE ELVORA METHOD",
        subtitleNote = "Step 3 of 7 — Rule-based local intelligence diagnoses your weak spots.",
        icon = Icons.Filled.Psychology
    ),
    WelcomeSlide(
        headline = "Create MCQs, FlipCards, Smart Summaries and Quick Answers.",
        whyElvoraTag = "FREE CORE LEARNING SUITE",
        subtitleNote = "Step 4 of 7 — Four free tools ready on your main dashboard.",
        icon = Icons.Filled.School
    ),
    WelcomeSlide(
        headline = "Go Premium for SmartNotes, Custom Quizzes, Quick Revision and StudyCast.",
        whyElvoraTag = "PREMIUM PREVIEW TOOLS",
        subtitleNote = "Step 5 of 7 — Copy-style notes, custom-marks quizzes, cheat boxes & audio.",
        icon = Icons.Filled.AutoAwesome
    ),
    WelcomeSlide(
        headline = "Learn smarter. Understand better. Achieve more.",
        whyElvoraTag = "ACTIVE RECALL + DIAGNOSIS",
        subtitleNote = "Step 6 of 7 — Every quiz tracks mistakes and builds targeted revision.",
        icon = Icons.Filled.Psychology
    ),
    WelcomeSlide(
        headline = "ELVORA — Modern Frameworks for a Modern World.",
        whyElvoraTag = "WELCOME ABOARD",
        subtitleNote = "Step 7 of 7 — Ready to begin your study session.",
        icon = Icons.Filled.School
    )
)

@Composable
fun WelcomeAnimationScreen(
    studentName: String,
    musicEnabled: Boolean,
    musicVolume: Float,
    reducedMotion: Boolean,
    ttsReady: Boolean,
    onStartMusic: () -> Unit,
    onToggleMusic: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSpeakText: (String) -> Unit,
    onStopSpeech: () -> Unit,
    onCompleteOrSkip: () -> Unit
) {
    val tokens = LocalElvoraTokens.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var slideElapsedMs by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var ttsNarrationEnabled by remember { mutableStateOf(false) }
    var lastSpokenIndex by remember { mutableIntStateOf(-1) }
    var localVolume by remember(musicVolume) { mutableFloatStateOf(musicVolume) }

    val slideDurationMs = 4400

    LaunchedEffect(Unit) {
        onStartMusic()
    }

    DisposableEffect(Unit) {
        onDispose { onStopSpeech() }
    }

    // Auto-playing sequential timeline that pauses cleanly & resumes from exact paused position
    LaunchedEffect(isPaused, currentIndex) {
        if (ttsNarrationEnabled && ttsReady && !isPaused && lastSpokenIndex != currentIndex) {
            lastSpokenIndex = currentIndex
            onSpeakText(welcomeSlides[currentIndex].headline)
        }
        while (!isPaused && currentIndex < welcomeSlides.size) {
            delay(100L)
            slideElapsedMs += 100
            if (slideElapsedMs >= slideDurationMs) {
                if (currentIndex < welcomeSlides.lastIndex) {
                    currentIndex++
                    slideElapsedMs = 0
                } else {
                    isPaused = true
                    slideElapsedMs = slideDurationMs
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "welcome_float")
    val orbShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_shift"
    )

    val slide = welcomeSlides[currentIndex]
    val overallProgress = ((currentIndex * slideDurationMs) + slideElapsedMs).toFloat() /
        (welcomeSlides.size * slideDurationMs).toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DeepMidnightBlue,
                        com.example.ui.theme.MidOceanBlue,
                        com.example.ui.theme.DarkMagenta
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Subtle animated study-themed background canvas (disabled when Reduced Motion is active)
        if (!reducedMotion) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = SoftSkyBlue.copy(alpha = 0.14f),
                    radius = size.minDimension * 0.34f,
                    center = Offset(
                        x = size.width * (0.2f + 0.15f * orbShift),
                        y = size.height * 0.22f
                    )
                )
                drawCircle(
                    color = SoftPinkGlow.copy(alpha = 0.16f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(
                        x = size.width * (0.82f - 0.16f * orbShift),
                        y = size.height * 0.72f
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Bar: Why ELVORA + Skip + Audio Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${studentName.ifBlank { "Scholar" }}",
                            style = MaterialTheme.typography.labelLarge,
                            color = SoftPinkGlow
                        )
                        Text(
                            text = "Why ELVORA?",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = onCompleteOrSkip,
                        modifier = Modifier.testTag("welcome_skip_button")
                    ) {
                        Text("Skip", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.SkipNext, contentDescription = "Skip welcome", tint = Color.White)
                    }
                }

                LinearProgressIndicator(
                    progress = { overallProgress.coerceIn(0f, 1f) },
                    color = VividMagentaPink,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )

                // Visual Study Illustration Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_hero),
                        contentDescription = "ELVORA Animated Study Illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = DeepMidnightBlue.copy(alpha = 0.76f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = SoftPinkGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = slide.whyElvoraTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                // Sequential Animated Headline & Subtitles Card
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = slide,
                        transitionSpec = {
                            if (reducedMotion) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                            else fadeIn(tween(380)) togetherWith fadeOut(tween(240))
                        },
                        label = "welcome_slide_content"
                    ) { activeSlide ->
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = activeSlide.headline,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Surface(
                                color = DeepMidnightBlue.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Subtitle: ${activeSlide.subtitleNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.92f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Step Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    welcomeSlides.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (idx == currentIndex) 11.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentIndex) VividMagentaPink
                                    else Color.White.copy(alpha = 0.35f)
                                )
                        )
                    }
                }

                // Playback Controls: Pause / Resume (keeps exact position!), Restart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { isPaused = !isPaused },
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("welcome_pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPaused) "Resume animation" else "Pause animation"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPaused) "Resume" else "Pause")
                    }

                    OutlinedButton(
                        onClick = {
                            currentIndex = 0
                            slideElapsedMs = 0
                            isPaused = false
                            lastSpokenIndex = -1
                        },
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("welcome_restart_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Restart animation")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restart")
                    }
                }

                // Dreamy Synth Music + Optional Device TTS Narration Panel
                Surface(
                    color = DeepMidnightBlue.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onToggleMusic) {
                                    Icon(
                                        imageVector = if (musicEnabled) Icons.Filled.MusicNote else Icons.Filled.MusicOff,
                                        contentDescription = "Mute or unmute music",
                                        tint = if (musicEnabled) SoftPinkGlow else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Retro Indie Lo-Fi",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (musicEnabled) "Playing • Tap icon to turn off anytime" else "Off • Tap icon to play anytime",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.72f)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "TTS Voice",
                                    tint = SoftSkyBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Voice", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = ttsNarrationEnabled,
                                    onCheckedChange = { enabled ->
                                        ttsNarrationEnabled = enabled
                                        if (!enabled) onStopSpeech()
                                        else if (ttsReady) onSpeakText(slide.headline)
                                    }
                                )
                            }
                        }

                        if (musicEnabled) {
                            Slider(
                                value = localVolume,
                                onValueChange = {
                                    localVolume = it
                                    onVolumeChange(it)
                                },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = VividMagentaPink,
                                    activeTrackColor = VividMagentaPink,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                )
                            )
                        }
                    }
                }

                // Start Learning CTA
                Button(
                    onClick = onCompleteOrSkip,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividMagentaPink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_learning_button")
                ) {
                    Text(
                        text = "Start Learning →",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
