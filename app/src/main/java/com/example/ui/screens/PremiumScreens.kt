package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.TtsLine
import com.example.intelligence.DifficultyLevel
import com.example.intelligence.ElvoraIntelligenceEngine
import com.example.intelligence.SampleStudyNotes
import com.example.intelligence.StudyCastLanguage
import com.example.ui.components.ElvoraSectionCard
import com.example.ui.components.LockedPremiumBadge
import com.example.ui.components.RevisionBoxesView
import com.example.ui.components.RuledCopyNotebookView
import com.example.ui.theme.DeepMidnightBlue
import com.example.ui.theme.LocalElvoraTokens
import com.example.ui.theme.MidOceanBlue
import com.example.ui.theme.VividMagentaPink

enum class PremiumFeatureType(
    val title: String,
    val tagline: String,
    val description: String,
    val benefits: List<String>,
    val howItWorks: List<String>
) {
    SMART_NOTES(
        title = "SmartNotes",
        tagline = "Complex topics. Simplified intelligently.",
        description = "Transforms dense chapters into clean, copy-notebook ruled sheets with dark broad topic bars, multi-color highlighter strokes, definitions, tricks, and sticky notes.",
        benefits = listOf(
            "Copy-style ruled pages with pink margin & dark topic banners",
            "Automatic highlighter emphasis on critical concepts & formulas",
            "Points to remember, memory tricks & personal sticky notes"
        ),
        howItWorks = listOf(
            "1. Paste notes or attach PDF / Image study material",
            "2. ELVORA structures topics, definitions, formulas & tricks",
            "3. Save formatted copy sheets to your Library"
        )
    ),
    QUIZ_FORGE(
        title = "QuizForge",
        tagline = "Your exam. Your rules. Your challenge.",
        description = "Build custom-marks exams (10–100 marks) by choosing exact counts of MCQs (1 mark), True/False (1 mark), and Short Answer (2 marks) questions.",
        benefits = listOf(
            "Exact mathematical mark validation (MCQ=1, T/F=1, Short=2)",
            "Customise question counts & difficulty (Easy, Medium, Hard)",
            "Instant evaluation, corrections & performance saving"
        ),
        howItWorks = listOf(
            "1. Set MCQ, True/False & Short Answer question counts",
            "2. Verify total marks (10 to 100 marks)",
            "3. Attempt custom exam & review Learning Diagnosis"
        )
    ),
    LAST_MINUTE_REVISION(
        title = "LastMinute Revision",
        tagline = "Everything important. Nothing unnecessary.",
        description = "Condenses study material into ultra-clean 1–2 page boxed cheat-sheets (max 5 pages) with color-coded topic boxes, directional arrows, formulas, and crisp recall bullets.",
        benefits = listOf(
            "Distinct Blue, Pink & Purple exam boxes for visual memory",
            "Directional concept arrows (→) & formula summary strips",
            "Zero fluff — built for fast pre-exam scanning"
        ),
        howItWorks = listOf(
            "1. Supply chapter notes or formulas",
            "2. Engine condenses into color-coded revision boxes",
            "3. Review 1–2 page high-yield sheet before exams"
        )
    ),
    STUDY_CAST(
        title = "StudyCast",
        tagline = "Turn your study material into conversations.",
        description = "Converts study notes into a natural two-speaker dialogue between Maya and Alex in English or Hindi with speed-adjustable playback.",
        benefits = listOf(
            "Two-speaker Maya & Alex Q&A format for auditory learners",
            "Playback speed options: 0.5x, 1x, 1.25x, 1.5x, and 2x",
            "Play, Pause, Resume & full readable transcript"
        ),
        howItWorks = listOf(
            "1. Paste your study passage",
            "2. Maya asks clarifying questions while Alex breaks concepts down",
            "3. Listen using device Text-to-Speech or read the transcript"
        )
    )
}

@Composable
fun PremiumFeatureGateScreen(
    feature: PremiumFeatureType,
    ttsReady: Boolean,
    ttsSpeaking: Boolean,
    ttsActiveLine: Int,
    onPlayTtsDialogue: (List<TtsLine>, Float) -> Unit,
    onPauseTts: () -> Unit,
    onResumeTts: () -> Unit,
    onStopTts: () -> Unit,
    onSetTtsRate: (Float) -> Unit,
    onOpenPlans: () -> Unit,
    onBack: () -> Unit
) {
    val tokens = LocalElvoraTokens.current

    DisposableEffect(feature) {
        onDispose { onStopTts() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = feature.title, onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Locked Hero Introduction Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(tokens.heroBannerBrush)
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = VividMagentaPink,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PREMIUM • LOCKED", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                                Text("Not for Free Tier", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            }
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "\"${feature.tagline}\"",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.92f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.84f)
                            )
                        }
                    }
                }

                // Benefits & How It Works
                item {
                    ElvoraSectionCard {
                        Text("Key Benefits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        feature.benefits.forEach { b ->
                            Text("• $b", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("How It Works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        feature.howItWorks.forEach { step ->
                            Text(step, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Interactive Visual Preview (Strictly Read-Only Preview — never unlocks Premium!)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Interactive Visual Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LockedPremiumBadge()
                    }
                }

                when (feature) {
                    PremiumFeatureType.SMART_NOTES -> {
                        item { SmartNotesVisualPreview() }
                    }
                    PremiumFeatureType.QUIZ_FORGE -> {
                        item { QuizForgeVisualPreview() }
                    }
                    PremiumFeatureType.LAST_MINUTE_REVISION -> {
                        item { LastMinuteRevisionVisualPreview() }
                    }
                    PremiumFeatureType.STUDY_CAST -> {
                        item {
                            StudyCastVisualPreview(
                                ttsReady = ttsReady,
                                ttsSpeaking = ttsSpeaking,
                                ttsActiveLine = ttsActiveLine,
                                onPlay = onPlayTtsDialogue,
                                onPause = onPauseTts,
                                onResume = onResumeTts,
                                onStop = onStopTts,
                                onSetRate = onSetTtsRate
                            )
                        }
                    }
                }

                // Unlock Information & Subscription Plans CTA (No fake unlock!)
                item {
                    ElvoraSectionCard(borderColor = VividMagentaPink) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = VividMagentaPink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Premium features require a verified subscription validated server-side. Payment collection is disabled in this ₹0 Zero-Budget Demo build. For subscription & payment inquiries, contact Instagram: @elvora.axiom.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenPlans,
                            colors = ButtonDefaults.buttonColors(containerColor = VividMagentaPink),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("view_subscription_plans_button")
                        ) {
                            Text("View Subscription Plans (₹49 – ₹999)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartNotesVisualPreview() {
    val tokens = LocalElvoraTokens.current
    var selectedUploadNote by remember { mutableStateOf<String?>(null) }

    // Zero-permission Photo Picker & Document Picker (Google Play Policy compliant)
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedUploadNote = if (uri != null) {
            "Image selected ($uri). Honest disclosure: Offline OCR text extraction from images is unavailable in the ₹0 zero-budget build — no fabricated text is generated."
        } else null
    }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedUploadNote = if (uri != null) {
            "PDF selected ($uri). Honest disclosure: Full binary PDF parsing requires a dedicated PDF engine or server extraction unavailable in this ₹0 offline build."
        } else null
    }

    val previewDoc = remember {
        ElvoraIntelligenceEngine.buildSmartNotesPreview(SampleStudyNotes.physicsLaws)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElvoraSectionCard {
            Text("Upload Interface Preview (Zero-Permission Pickers)", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Upload", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Image Upload", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (selectedUploadNote != null) {
                Surface(color = tokens.noticeContainer, shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = selectedUploadNote!!,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Authentic Ruled Copy Notebook Preview
        RuledCopyNotebookView(doc = previewDoc)
    }
}

@Composable
private fun QuizForgeVisualPreview() {
    var mcqCount by remember { mutableIntStateOf(4) }
    var tfCount by remember { mutableIntStateOf(2) }
    var shortCount by remember { mutableIntStateOf(2) }
    var difficulty by remember { mutableStateOf(DifficultyLevel.MEDIUM) }

    val totalMarks = (mcqCount * 1) + (tfCount * 1) + (shortCount * 2)
    val isValidMarks = totalMarks in 10..100
    val previewQuestions = remember(mcqCount, tfCount, shortCount, difficulty) {
        ElvoraIntelligenceEngine.buildQuizForgePreview(
            text = SampleStudyNotes.physicsLaws,
            mcqCount = mcqCount,
            tfCount = tfCount,
            shortCount = shortCount,
            difficulty = difficulty
        )
    }

    ElvoraSectionCard {
        Text("Customise Exam Marks & Question Mix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "MCQ = 1 mark • True/False = 1 mark • Short Answer = 2 marks (Target: 10–100 marks)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CounterRow("MCQs (1 mark each)", mcqCount, onDec = { if (mcqCount > 0) mcqCount-- }, onInc = { if (mcqCount < 20) mcqCount++ })
        CounterRow("True / False (1 mark each)", tfCount, onDec = { if (tfCount > 0) tfCount-- }, onInc = { if (tfCount < 20) tfCount++ })
        CounterRow("Short Answer (2 marks each)", shortCount, onDec = { if (shortCount > 0) shortCount-- }, onInc = { if (shortCount < 15) shortCount++ })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DifficultyLevel.entries.forEach { d ->
                FilterChip(
                    selected = difficulty == d,
                    onClick = { difficulty = d },
                    label = { Text(d.label) }
                )
            }
        }

        Surface(
            color = if (isValidMarks) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Calculated Total Marks: $totalMarks / 100 " +
                    if (isValidMarks) "(Valid: ${mcqCount}×1 + ${tfCount}×1 + ${shortCount}×2)"
                    else "(Adjust counts so total marks is between 10 and 100)",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(12.dp)
            )
        }

        Text("Sample Generated Paper Preview (First 3 Questions):", style = MaterialTheme.typography.labelLarge)
        previewQuestions.take(3).forEachIndexed { idx, q ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Q${idx + 1} [${q.kind.label} • ${q.kind.marks}m]",
                        style = MaterialTheme.typography.labelSmall,
                        color = VividMagentaPink
                    )
                    Text(text = q.prompt, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun CounterRow(label: String, count: Int, onDec: () -> Unit, onInc: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDec) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease $label")
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            IconButton(onClick = onInc) {
                Icon(Icons.Filled.Add, contentDescription = "Increase $label")
            }
        }
    }
}

@Composable
private fun LastMinuteRevisionVisualPreview() {
    var useBiology by remember { mutableStateOf(false) }
    val doc = remember(useBiology) {
        ElvoraIntelligenceEngine.buildRevisionSheetPreview(
            if (useBiology) SampleStudyNotes.cellBiology else SampleStudyNotes.physicsLaws
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !useBiology,
                onClick = { useBiology = false },
                label = { Text("Sample: Physics Laws") }
            )
            FilterChip(
                selected = useBiology,
                onClick = { useBiology = true },
                label = { Text("Sample: Cell Biology") }
            )
        }
        RevisionBoxesView(doc = doc)
    }
}

@Composable
private fun StudyCastVisualPreview(
    ttsReady: Boolean,
    ttsSpeaking: Boolean,
    ttsActiveLine: Int,
    onPlay: (List<TtsLine>, Float) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSetRate: (Float) -> Unit
) {
    val tokens = LocalElvoraTokens.current
    var selectedLanguage by remember { mutableStateOf(StudyCastLanguage.ENGLISH) }
    val episode = remember(selectedLanguage) {
        ElvoraIntelligenceEngine.buildStudyCastPreview(SampleStudyNotes.cellBiology, selectedLanguage)
    }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }
    var hasStarted by remember { mutableStateOf(false) }

    val ttsLines = remember(episode) {
        episode.turns.mapIndexed { idx, turn ->
            val isMaya = turn.speaker == "Maya"
            TtsLine(
                id = "sc_${selectedLanguage.name}_$idx",
                speaker = turn.speaker,
                text = turn.text,
                pitch = if (isMaya) 1.22f else 0.84f,
                rateFactor = if (isMaya) 0.94f else 0.91f,
                localeTag = selectedLanguage.localeTag
            )
        }
    }

    ElvoraSectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Headphones, contentDescription = null, tint = VividMagentaPink)
            Spacer(modifier = Modifier.width(8.dp))
            Text("StudyCast Preview: ${episode.topic}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        // English & Hindi Podcast Toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StudyCastLanguage.entries.forEach { lang ->
                FilterChip(
                    selected = selectedLanguage == lang,
                    onClick = {
                        selectedLanguage = lang
                        hasStarted = false
                        onStop()
                    },
                    label = { Text(lang.label) }
                )
            }
        }

        Surface(color = tokens.blueBoxBg, shape = RoundedCornerShape(10.dp)) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MidOceanBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Maya & Alex speak this study dialogue in English or Hindi using built-in Android Text-to-Speech.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Speed selector: 0.5x, 1x, 1.25x, 1.5x, 2x
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                FilterChip(
                    selected = selectedSpeed == spd,
                    onClick = {
                        selectedSpeed = spd
                        onSetRate(spd)
                    },
                    label = { Text("${spd}x", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (!hasStarted) {
                        hasStarted = true
                        onPlay(ttsLines, selectedSpeed)
                    } else if (ttsSpeaking) {
                        onPause()
                    } else {
                        onResume()
                    }
                },
                enabled = ttsReady,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (ttsSpeaking) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    when {
                        ttsSpeaking -> "Pause"
                        hasStarted -> "Resume"
                        else -> "Play ${selectedLanguage.label}"
                    }
                )
            }
            OutlinedButton(
                onClick = {
                    hasStarted = false
                    onStop()
                }
            ) {
                Icon(Icons.Filled.Stop, contentDescription = "Stop")
            }
        }

        // Maya & Alex Dialogue Transcript
        episode.turns.forEachIndexed { idx, turn ->
            val isMaya = turn.speaker == "Maya"
            val isCurrent = idx == ttsActiveLine
            Surface(
                color = if (isMaya) tokens.pinkBoxBg else tokens.blueBoxBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (isCurrent) 2.dp else 1.dp,
                    color = if (isCurrent) VividMagentaPink else (if (isMaya) tokens.pinkBoxBorder else tokens.blueBoxBorder).copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isMaya) "MAYA" else "ALEX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isMaya) VividMagentaPink else MidOceanBlue
                    )
                    Text(text = turn.text, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private data class PlanSpec(
    val title: String,
    val price: String,
    val duration: String,
    val badge: String? = null,
    val perDay: String? = null,
    val perMonth: String? = null,
    val note: String? = null,
    val isHighlighted: Boolean = false
)

@Composable
fun SubscriptionPlansScreen(onBack: () -> Unit) {
    val tokens = LocalElvoraTokens.current
    val plans = listOf(
        PlanSpec("7 Days", "₹49", "7 days"),
        PlanSpec("14 Days", "₹79", "14 days"),
        PlanSpec("1 Month", "₹149", "30 days"),
        PlanSpec(
            title = "3 Months",
            price = "₹399",
            duration = "90 days",
            badge = "MOST POPULAR",
            perDay = "₹4.43/day",
            perMonth = "₹133/month",
            isHighlighted = true
        ),
        PlanSpec(
            title = "6 Months",
            price = "₹599",
            duration = "180 days",
            badge = "BEST VALUE",
            perDay = "₹3.33/day",
            perMonth = "₹99.83/month",
            note = "Just ₹200 more than the 3-month plan for 3 extra months.",
            isHighlighted = true
        ),
        PlanSpec(
            title = "12 Months",
            price = "₹999",
            duration = "365 days",
            badge = "BIGGEST SAVINGS",
            perDay = "₹2.74/day",
            perMonth = "₹83.25/month",
            note = "Just ₹400 more than the 6-month plan for 6 additional months.",
            isHighlighted = true
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "Subscription Plans", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = tokens.noticeContainer,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, tokens.noticeColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Display-Only Pricing Preview (₹0 Zero-Budget Mode)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No payment is collected inside the app and purchases are not simulated. For payment inquiries & support, contact Instagram: @elvora.axiom.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                item {
                    ElvoraSectionCard(borderColor = VividMagentaPink) {
                        Text(
                            text = "What Premium Learner Unlocks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VividMagentaPink
                        )
                        Text(
                            text = "• Unlimited Smart MCQs & Custom FlipCards: Generate all possible questions & flashcards from full-page notes (Free tier capped at 10).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Photo & File Upload: Import photos and study files across Smart MCQs, FlipCards, Smart Summary & Quick Answers.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Full Premium Suite: SmartNotes copy sheets, QuizForge custom-marks exams, LastMinute Revision & StudyCast podcasts.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                items(plans.size) { index ->
                    val plan = plans[index]
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (plan.isHighlighted) tokens.pinkBoxBg else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (plan.isHighlighted) 1.8.dp else 1.dp,
                            color = if (plan.isHighlighted) VividMagentaPink else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(plan.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (plan.badge != null) {
                                    Surface(color = VividMagentaPink, shape = RoundedCornerShape(50)) {
                                        Text(
                                            text = plan.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = plan.price,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = if (plan.isHighlighted) VividMagentaPink else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "/ ${plan.duration}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            if (plan.perDay != null && plan.perMonth != null) {
                                Text(
                                    text = "${plan.perDay} • ${plan.perMonth} • ${plan.duration} • Total ${plan.price}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (plan.note != null) {
                                Text(
                                    text = plan.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = DeepMidnightBlue.takeIf { !tokens.isDark } ?: Color.White
                                )
                            }
                        }
                    }
                }

                // Payment Contact & Future Payment Architecture Spec Card
                item {
                    ElvoraSectionCard(borderColor = VividMagentaPink) {
                        Text("Payment & Unlock Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Instagram: @elvora.axiom\nReach out directly for plan details, support, and subscription assistance.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = VividMagentaPink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Future Payment & Verification Architecture", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "• Server-side payment order & cryptographic signature confirmation\n" +
                                "• Subscription activation strictly after verified server webhook\n" +
                                "• Expiry timestamp & active plan synced to user profile\n" +
                                "• Row-level server-side Premium access protection (never client-unlocked)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
