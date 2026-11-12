package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intelligence.RevisionSheetDocument
import com.example.intelligence.SmartNotesDocument
import com.example.ui.theme.DeepMidnightBlue
import com.example.ui.theme.DeepRosePink
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LightBlushPink
import com.example.ui.theme.LocalElvoraTokens
import com.example.ui.theme.MidOceanBlue
import com.example.ui.theme.SoftPinkGlow
import com.example.ui.theme.SoftSkyBlue
import com.example.ui.theme.VelvetViolet
import com.example.ui.theme.VividMagentaPink

@Composable
fun ElvoraBrandBanner(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val tokens = LocalElvoraTokens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(tokens.primaryGradient)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailing()
            }
        }
    }
}

@Composable
fun MusicMutePill(
    musicEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = if (musicEnabled) VividMagentaPink.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (musicEnabled) VividMagentaPink else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.testTag("music_mute_toggle")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (musicEnabled) Icons.Filled.MusicNote else Icons.Filled.MusicOff,
                contentDescription = if (musicEnabled) "Mute background music" else "Play background music",
                tint = if (musicEnabled) VividMagentaPink else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (musicEnabled) "Retro Indie" else "Music Off",
                style = MaterialTheme.typography.labelMedium,
                color = if (musicEnabled) VividMagentaPink else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DemoModeBanner(
    text: String = "Local Device Mode (₹0 Zero-Budget) • Stored safely on this device, not cloud-synced.",
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Mode info",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HighlightedSentence(
    text: String,
    highlightColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onHighlightColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val words = text.split(" ")
    val splitIndex = (words.size / 2).coerceAtLeast(1)
    val lead = words.take(splitIndex).joinToString(" ")
    val rest = words.drop(splitIndex).joinToString(" ")
    val onHighlight = onHighlightColor ?: if (LocalElvoraTokens.current.isDark) Color.White else DeepMidnightBlue

    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    background = highlightColor,
                    color = onHighlight,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(" $lead ")
            }
            if (rest.isNotEmpty()) {
                append(" $rest")
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = textColor,
        modifier = modifier
    )
}

/**
 * White copy-notebook ruled sheet for SmartNotes with horizontal rules, soft pink left margin,
 * dark broad topic banner, Pink & Purple highlighted key points, definitions, points to remember,
 * tricks, and soft pink sticky note.
 */
@Composable
fun RuledCopyNotebookView(
    doc: SmartNotesDocument,
    modifier: Modifier = Modifier
) {
    val tokens = LocalElvoraTokens.current
    val paperText = DeepMidnightBlue // Always crisp dark ink on white notebook paper

    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 3.dp,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.notebookPaper)
        ) {
            // Ruled copy lines + pink margin line drawn dynamically via Canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                val lineSpacing = 28.dp.toPx()
                val marginX = 36.dp.toPx()
                var y = lineSpacing
                while (y < size.height) {
                    drawLine(
                        color = tokens.notebookRuleLine,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += lineSpacing
                }
                // Double copy-style vertical margin line in pink/magenta
                drawLine(
                    color = tokens.notebookMarginLine,
                    start = Offset(marginX, 0f),
                    end = Offset(marginX, size.height),
                    strokeWidth = 1.6.dp.toPx()
                )
                drawLine(
                    color = tokens.notebookMarginLine.copy(alpha = 0.45f),
                    start = Offset(marginX + 3.dp.toPx(), 0f),
                    end = Offset(marginX + 3.dp.toPx(), size.height),
                    strokeWidth = 0.9.dp.toPx()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                doc.sections.forEach { section ->
                    // Dark broad topic bar (as specifically requested!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DeepMidnightBlue)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = section.topicTitle.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = Color.White
                        )
                    }

                    // Key Highlighted Points (Pink & Purple Highlighter Markers on White Copy Sheet)
                    if (section.highlightedPoints.isNotEmpty()) {
                        Text(
                            text = "KEY CONCEPTS (PINK & PURPLE HIGHLIGHTS)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepRosePink
                        )
                        section.highlightedPoints.forEachIndexed { idx, pt ->
                            val hlColor = if (idx % 2 == 0) tokens.highlighterPink else tokens.highlighterPurple
                            Row(verticalAlignment = Alignment.Top) {
                                Text("• ", color = DeepRosePink, fontWeight = FontWeight.Bold)
                                HighlightedSentence(
                                    text = pt,
                                    highlightColor = hlColor,
                                    textColor = paperText,
                                    onHighlightColor = DeepMidnightBlue
                                )
                            }
                        }
                    }

                    // Definitions
                    if (section.definitions.isNotEmpty()) {
                        Text(
                            text = "DEFINITIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidOceanBlue
                        )
                        section.definitions.forEach { def ->
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MidOceanBlue.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = def.term,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = paperText
                                    )
                                    Text(
                                        text = def.definition,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = paperText.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Formulas
                    if (section.formulas.isNotEmpty()) {
                        Text(
                            text = "IMPORTANT FORMULAS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VelvetViolet
                        )
                        section.formulas.forEach { f ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tokens.highlighterPurple.copy(alpha = 0.72f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = f,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = paperText
                                )
                            }
                        }
                    }

                    // Points to Remember
                    if (section.pointsToRemember.isNotEmpty()) {
                        Text(
                            text = "POINTS TO REMEMBER",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepMidnightBlue
                        )
                        section.pointsToRemember.forEach { pt ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = null,
                                    tint = VividMagentaPink,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = paperText
                                )
                            }
                        }
                    }

                    // Tricks & Memory Hooks
                    if (section.memoryTricks.isNotEmpty()) {
                        Surface(
                            color = LightBlushPink,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DeepRosePink.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Lightbulb,
                                        contentDescription = null,
                                        tint = DeepRosePink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "MEMORY TRICKS",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepRosePink
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                section.memoryTricks.forEach { trick ->
                                    Text(
                                        text = "• $trick",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = paperText
                                    )
                                }
                            }
                        }
                    }
                }

                // Personal Sticky Note Card (Soft Pink & Purple Accent — Zero Orange/Yellow)
                Surface(
                    color = tokens.highlighterPink,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DeepRosePink.copy(alpha = 0.35f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = doc.stickyNote,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepMidnightBlue,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * LastMinute Revision cheat-sheet styled exclusively with Blue (Dark/Light Blue)
 * and Magenta-Pink (Dark/Light Pink) organized study-note blocks, dark header tabs,
 * directional arrows (→), and formula strips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RevisionBoxesView(
    doc: RevisionSheetDocument,
    modifier: Modifier = Modifier
) {
    val tokens = LocalElvoraTokens.current
    val darkBlueBlock = Color(0xFF0F1B59)
    val midBlueAccent = Color(0xFF1D358A)
    val lightBlueBlock = if (tokens.isDark) Color(0xFF142466) else Color(0xFFEAF2FF)
    val lightBlueBorder = if (tokens.isDark) Color(0xFF60A5FA) else Color(0xFF1D358A)

    val darkPinkBlock = Color(0xFF7A1242)
    val midPinkAccent = Color(0xFFA61453)
    val lightPinkBlock = if (tokens.isDark) Color(0xFF541444) else Color(0xFFF7D0E4)
    val lightPinkBorder = if (tokens.isDark) Color(0xFFD63B84) else Color(0xFFA61453)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) Color(0xFF0A1030) else Color(0xFFFFFBFD),
        border = BorderStroke(1.5.dp, lightBlueBorder.copy(alpha = 0.45f)),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Marked Topic Header Banner (Dark Blue -> Dark Magenta Pink)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(darkBlueBlock, midBlueAccent, darkPinkBlock)
                        )
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LAST-MINUTE REVISION SHEET",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE05297),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = doc.mainTopic,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${doc.estimatedPages} • Blue & Magenta Study Blocks",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDBEAFE)
                    )
                }
                Surface(color = VividMagentaPink, shape = RoundedCornerShape(50)) {
                    Text(
                        text = "HIGH YIELD",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Formulas Block (Dark Blue Tab + Light Blue Body)
            if (doc.formulasStrip.isNotEmpty()) {
                Surface(
                    color = lightBlueBlock,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, lightBlueBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(darkBlueBlock)
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "KEY FORMULAS & EQUATIONS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF93C5FD)
                            )
                        }
                        FlowRow(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            doc.formulasStrip.forEach { formula ->
                                Surface(
                                    color = if (tokens.isDark) darkBlueBlock else Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, lightBlueBorder.copy(alpha = 0.65f))
                                ) {
                                    Text(
                                        text = formula,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (tokens.isDark) Color(0xFFDBEAFE) else darkBlueBlock,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Organized Study-Note Blocks (Dark/Light Blue & Dark/Light Pink)
            doc.boxes.forEach { box ->
                val isDarkBlock = box.boxStyle == "DARK_BLUE" || box.boxStyle == "DARK_PINK"
                val isBlueFamily = box.boxStyle == "DARK_BLUE" || box.boxStyle == "LIGHT_BLUE" || box.boxStyle == "BLUE"

                val containerBg = when (box.boxStyle) {
                    "DARK_BLUE" -> darkBlueBlock
                    "DARK_PINK" -> darkPinkBlock
                    "LIGHT_PINK", "PINK" -> lightPinkBlock
                    else -> lightBlueBlock
                }
                val tabBg = when {
                    box.boxStyle == "DARK_BLUE" -> midBlueAccent
                    box.boxStyle == "DARK_PINK" -> midPinkAccent
                    isBlueFamily -> darkBlueBlock
                    else -> darkPinkBlock
                }
                val borderColor = when {
                    isBlueFamily -> lightBlueBorder
                    else -> lightPinkBorder
                }
                val bodyTextColor = when {
                    isDarkBlock -> Color.White
                    tokens.isDark -> Color(0xFFF8FAFC)
                    else -> Color(0xFF0B1136)
                }
                val arrowTint = when {
                    box.boxStyle == "DARK_BLUE" -> Color(0xFF93C5FD)
                    box.boxStyle == "DARK_PINK" -> Color(0xFFE05297)
                    isBlueFamily -> lightBlueBorder
                    else -> lightPinkBorder
                }

                Surface(
                    color = containerBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Dark Broad Topic Tab Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(tabBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isBlueFamily) Color(0xFF93C5FD) else Color(0xFFE05297))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = box.title.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = if (isBlueFamily) "BLUE NOTE" else "PINK NOTE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }

                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!box.arrowFlow.isNullOrBlank()) {
                                Surface(
                                    color = if (isDarkBlock) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = if (tokens.isDark) 0.1f else 0.85f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, arrowTint.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = arrowTint,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = box.arrowFlow,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = bodyTextColor
                                        )
                                    }
                                }
                            }

                            box.items.forEach { item ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("→ ", color = arrowTint, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = bodyTextColor,
                                        fontWeight = if (isDarkBlock) FontWeight.Medium else FontWeight.Normal
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

/**
 * 3D FlipCard styled exclusively with Pink and Blue across both Light & Dark themes,
 * respecting Reduced Motion.
 */
@Composable
fun FlipCard3D(
    frontText: String,
    backText: String,
    memoryCue: String,
    isFlipped: Boolean,
    isLearnt: Boolean,
    reducedMotion: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalElvoraTokens.current
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 420),
        label = "flip_card_rotation"
    )

    val showBack = rotation > 90f
    val frontInk = if (tokens.isDark) Color(0xFFF0F6FF) else DeepMidnightBlue
    val backInk = if (tokens.isDark) Color(0xFFFFF1F7) else DeepMidnightBlue

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (showBack) tokens.flipCardBack else tokens.flipCardFront
        ),
        border = BorderStroke(
            width = 1.6.dp,
            color = if (isLearnt) tokens.successColor else if (showBack) tokens.pinkBoxBorder else tokens.blueBoxBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .graphicsLayer {
                if (!reducedMotion) {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
            }
            .clickable {
                com.example.audio.TapSoundPlayer.playCardTap()
                onFlip()
            }
            .testTag("flipcard_surface")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .graphicsLayer {
                    if (!reducedMotion && showBack) {
                        rotationY = 180f
                    }
                }
        ) {
            if (!showBack) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (tokens.isDark) MidOceanBlue.copy(alpha = 0.45f) else Color.White,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, tokens.blueBoxBorder.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "PROMPT • BLUE FRONT",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tokens.isDark) SoftSkyBlue else MidOceanBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        if (isLearnt) {
                            Surface(color = tokens.successContainer, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "Learnt ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.successColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = frontText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = frontInk,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Tap card to flip →",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = if (tokens.isDark) DeepRosePink.copy(alpha = 0.45f) else Color.White,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, tokens.pinkBoxBorder.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "ANSWER • PINK BACK",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = backText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = backInk,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Cue: $memoryCue",
                        style = MaterialTheme.typography.bodySmall,
                        color = backInk.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedScoreCounter(
    score: Int,
    total: Int,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 750),
        label = "score_counter"
    )
    val percent = if (total > 0) (animatedScore * 100) / total else 0
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$animatedScore / $total",
            style = MaterialTheme.typography.displayLarge,
            color = VividMagentaPink
        )
        Text(
            text = "$percent% Accuracy",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ElvoraSectionCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalElvoraTokens.current
    val borderStroke = if (borderColor != null) {
        BorderStroke(1.2.dp, borderColor)
    } else {
        BorderStroke(1.2.dp, tokens.cardBorderBrush)
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tokens.isDark) {
                Color(0xFF1A1E56).copy(alpha = 0.88f)
            } else {
                Color.White.copy(alpha = 0.90f)
            }
        ),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun LockedPremiumBadge(modifier: Modifier = Modifier) {
    Surface(
        color = VividMagentaPink.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, VividMagentaPink.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked Premium feature",
                tint = VividMagentaPink,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "LOCKED",
                style = MaterialTheme.typography.labelSmall,
                color = VividMagentaPink
            )
        }
    }
}
