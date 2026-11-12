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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ActivityLogEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.UserEntity
import com.example.ui.components.ElvoraSectionCard
import com.example.ui.theme.BlushPinkAccent
import com.example.ui.theme.DeepMidnightBlue
import com.example.ui.theme.DeepRosePink
import com.example.ui.theme.LocalElvoraTokens
import com.example.ui.theme.MidOceanBlue
import com.example.ui.theme.SoftPinkGlow
import com.example.ui.theme.SoftSkyBlue
import com.example.ui.theme.VividMagentaPink
import kotlinx.coroutines.delay

private data class FreeToolCardMeta(
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val isPinkCard: Boolean,
    val testTag: String,
    val onClick: () -> Unit
)

private data class DashboardAnimatedStep(
    val tag: String,
    val message: String,
    val caption: String
)

private val dashboardShowcaseSteps = listOf(
    DashboardAnimatedStep(
        tag = "WHY ELVORA?",
        message = "Finding learning difficult? Too much to study, but don't know where to start?",
        caption = "Clarity over overload — turn bulky topics into simple steps."
    ),
    DashboardAnimatedStep(
        tag = "SMART & ENJOYABLE",
        message = "No worries! ELVORA is here to make learning easier, smarter and more enjoyable.",
        caption = "Calm pink & blue academic focus built for students."
    ),
    DashboardAnimatedStep(
        tag = "THE ELVORA METHOD",
        message = "Introducing the ELVORA Method — turn your study material into effective learning experiences.",
        caption = "Local rule-based intelligence that diagnoses what to revise."
    ),
    DashboardAnimatedStep(
        tag = "4 FREE TOOLS",
        message = "Create MCQs, FlipCards, Smart Summaries and Quick Answers.",
        caption = "Available immediately on your dashboard at ₹0 cost."
    ),
    DashboardAnimatedStep(
        tag = "PREMIUM SUITE",
        message = "Go Premium for SmartNotes, Custom Quizzes, Quick Revision and StudyCast.",
        caption = "Preview copy-style notes, custom-marks quizzes & dual-voice podcasts."
    ),
    DashboardAnimatedStep(
        tag = "ACTIVE RECALL",
        message = "Learn smarter. Understand better. Achieve more.",
        caption = "Every attempt updates your real mistake tracker and analytics."
    ),
    DashboardAnimatedStep(
        tag = "ELVORA",
        message = "ELVORA — Modern Frameworks for a Modern World.",
        caption = "Making modern education simple, accessible & engaging."
    )
)

@Composable
fun DashboardScreen(
    user: UserEntity?,
    attempts: List<QuizAttemptEntity>,
    activities: List<ActivityLogEntity>,
    reducedMotion: Boolean,
    onOpenMcqs: () -> Unit,
    onOpenFlipCards: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenQuickAnswers: () -> Unit,
    onExplorePremium: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenProgress: () -> Unit,
    onReplayWelcome: () -> Unit
) {
    val tokens = LocalElvoraTokens.current

    // Auto-playing sequential ELVORA Method animation on the Dashboard hero card
    var currentSlideIndex by remember { mutableIntStateOf(0) }
    var isShowcasePaused by remember { mutableStateOf(false) }

    LaunchedEffect(isShowcasePaused) {
        while (!isShowcasePaused) {
            delay(3600L)
            currentSlideIndex = (currentSlideIndex + 1) % dashboardShowcaseSteps.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dashboard_hero_float")
    val orbFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dashboard_orb"
    )

    val freeTools = listOf(
        FreeToolCardMeta(
            title = "Smart MCQs",
            subtitle = "Turn notes into Easy, Medium or Hard quizzes with instant Learning Diagnosis.",
            badge = "FREE • 2/5/10 Qs",
            icon = Icons.Filled.Quiz,
            isPinkCard = false,
            testTag = "tool_card_mcqs",
            onClick = onOpenMcqs
        ),
        FreeToolCardMeta(
            title = "FlipCards",
            subtitle = "Active recall cards with pink & blue sides & learnt tracking.",
            badge = "FREE • 3D FLIP",
            icon = Icons.Filled.Flip,
            isPinkCard = true,
            testTag = "tool_card_flipcards",
            onClick = onOpenFlipCards
        ),
        FreeToolCardMeta(
            title = "Smart Summary",
            subtitle = "Organise notes into bullet points, definitions, keywords & formulas.",
            badge = "FREE • STRUCTURED",
            icon = Icons.Filled.Summarize,
            isPinkCard = true,
            testTag = "tool_card_summary",
            onClick = onOpenSummary
        ),
        FreeToolCardMeta(
            title = "Quick Answers",
            subtitle = "Ask a question on your pasted notes with honest uncertainty detection.",
            badge = "FREE • VERIFIED",
            icon = Icons.Filled.ElectricBolt,
            isPinkCard = false,
            testTag = "tool_card_quick_answers",
            onClick = onOpenQuickAnswers
        )
    )

    val totalActivities = activities.size
    val avgAccuracy = if (attempts.isNotEmpty()) {
        val totScore = attempts.sumOf { it.score }
        val totMax = attempts.sumOf { it.totalMarks }.coerceAtLeast(1)
        (totScore * 100) / totMax
    } else 0
    val cardsLearnt = activities.sumOf { it.cardsLearntCount }

    val activeSlide = dashboardShowcaseSteps[currentSlideIndex]

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated ELVORA Intro / Why ELVORA Showcase Card (replaces static "ELVORA Learner" banner)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(tokens.heroBannerBrush)
                        .testTag("dashboard_animated_hero")
                ) {
                    if (!reducedMotion) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                color = BlushPinkAccent.copy(alpha = 0.22f),
                                radius = size.minDimension * 0.42f,
                                center = Offset(
                                    x = size.width * (0.18f + 0.16f * orbFloat),
                                    y = size.height * 0.24f
                                )
                            )
                            drawCircle(
                                color = SoftSkyBlue.copy(alpha = 0.20f),
                                radius = size.minDimension * 0.36f,
                                center = Offset(
                                    x = size.width * (0.85f - 0.16f * orbFloat),
                                    y = size.height * 0.78f
                                )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, BlushPinkAccent.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = BlushPinkAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${activeSlide.tag} • ${currentSlideIndex + 1}/${dashboardShowcaseSteps.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isShowcasePaused = !isShowcasePaused },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isShowcasePaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                        contentDescription = if (isShowcasePaused) "Resume intro animation" else "Pause intro animation",
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = onReplayWelcome,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("dashboard_fullscreen_welcome_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fullscreen,
                                        contentDescription = "Open full screen welcome animation",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.img_student_avatar),
                                    contentDescription = "Open student profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onOpenProfile)
                                        .testTag("dashboard_profile_chip")
                                )
                            }
                        }

                        AnimatedContent(
                            targetState = activeSlide,
                            transitionSpec = {
                                if (reducedMotion) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                                else fadeIn(tween(420)) togetherWith fadeOut(tween(240))
                            },
                            label = "dashboard_welcome_anim"
                        ) { slide ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = slide.message,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = slide.caption,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (currentSlideIndex + 1).toFloat() / dashboardShowcaseSteps.size.toFloat() },
                            color = BlushPinkAccent,
                            trackColor = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user?.name?.takeIf { it.isNotBlank() && it != "ELVORA Learner" }
                                    ?.let { "Studying as $it" } ?: "ELVORA Study Hub • Free Core",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "Watch Full Intro →",
                                style = MaterialTheme.typography.labelMedium,
                                color = BlushPinkAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable(onClick = onReplayWelcome)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Learning Progress Snapshot (Pink & Blue Cards)
            item {
                ElvoraSectionCard(
                    borderColor = if (tokens.isDark) SoftPinkGlow.copy(alpha = 0.42f) else VividMagentaPink.copy(alpha = 0.28f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Learning Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View Analytics →",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onOpenProgress)
                                .padding(4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMiniBox(
                            label = "Activities",
                            value = totalActivities.toString(),
                            color = if (tokens.isDark) SoftSkyBlue else MidOceanBlue,
                            bg = tokens.blueBoxBg,
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniBox(
                            label = "Quiz Accuracy",
                            value = if (attempts.isEmpty()) "—" else "$avgAccuracy%",
                            color = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                            bg = tokens.pinkBoxBg,
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniBox(
                            label = "Cards Learnt",
                            value = cardsLearnt.toString(),
                            color = if (tokens.isDark) BlushPinkAccent else VividMagentaPink,
                            bg = tokens.pinkBoxBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ONLY Free Learning Tools Section Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Free Learning Tools",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powered by ELVORA Rule-Based Intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4 Free Tool Cards (2 Blue + 2 Pink for vibrant Pink & Blue harmony)
            items(freeTools.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tool ->
                        val cardBg = if (tool.isPinkCard) tokens.pinkBoxBg else tokens.blueBoxBg
                        val accentColor = if (tool.isPinkCard) {
                            if (tokens.isDark) SoftPinkGlow else DeepRosePink
                        } else {
                            if (tokens.isDark) SoftSkyBlue else MidOceanBlue
                        }
                        val borderColor = if (tool.isPinkCard) tokens.pinkBoxBorder else tokens.blueBoxBorder

                        Card(
                            onClick = tool.onClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.2.dp, borderColor.copy(alpha = 0.52f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag(tool.testTag)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(accentColor.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = tool.title,
                                            tint = accentColor
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = tool.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tool.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    minLines = 3
                                )
                                Surface(
                                    color = accentColor.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = tool.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Explore Premium CTA (Rich Pink & Blue Card)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = tokens.pinkBoxBg,
                    border = BorderStroke(1.2.dp, tokens.pinkBoxBorder.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.WorkspacePremium,
                                    contentDescription = null,
                                    tint = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Premium Suite Available",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tokens.isDark) SoftPinkGlow else DeepRosePink
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Preview SmartNotes copy sheets, QuizForge, LastMinute Revision & StudyCast in the menu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onExplorePremium,
                            colors = ButtonDefaults.buttonColors(containerColor = VividMagentaPink),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("explore_premium_button")
                        ) {
                            Text("Explore", color = Color.White)
                        }
                    }
                }
            }

            // Recent Activities (Real records only)
            item {
                ElvoraSectionCard(
                    borderColor = tokens.blueBoxBorder.copy(alpha = 0.42f)
                ) {
                    Text(
                        text = "Recent Activities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (activities.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = if (tokens.isDark) SoftPinkGlow else DeepRosePink
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "No activity recorded yet. Try Smart MCQs or FlipCards above to start tracking real progress!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        activities.take(5).forEachIndexed { idx, act ->
                            val rowBg = if (idx % 2 == 0) tokens.blueBoxBg else tokens.pinkBoxBg
                            Surface(
                                color = rowBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (idx % 2 == 0) tokens.blueBoxBorder.copy(alpha = 0.25f)
                                    else tokens.pinkBoxBorder.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${act.featureName} • ${act.topic}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = act.detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniBox(
    label: String,
    value: String,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
