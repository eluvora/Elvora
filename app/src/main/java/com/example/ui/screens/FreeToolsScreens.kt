package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.intelligence.DiagnosisReport
import com.example.intelligence.DifficultyLevel
import com.example.intelligence.ElvoraIntelligenceEngine
import com.example.intelligence.FlipCardItem
import com.example.intelligence.McqQuestion
import com.example.intelligence.QuickAnswerResult
import com.example.intelligence.SampleStudyNotes
import com.example.intelligence.SmartSummaryResult
import com.example.ui.components.AnimatedScoreCounter
import com.example.ui.components.ElvoraSectionCard
import com.example.ui.components.FlipCard3D
import com.example.ui.components.HighlightedSentence
import com.example.ui.theme.DeepRosePink
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalElvoraTokens
import com.example.ui.theme.SoftPinkGlow
import com.example.ui.theme.VividMagentaPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("tool_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun SampleNotesHelperRow(onLoadSample: (String) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { onLoadSample(SampleStudyNotes.physicsLaws) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Sample: Physics", style = MaterialTheme.typography.labelSmall)
        }
        OutlinedButton(
            onClick = { onLoadSample(SampleStudyNotes.cellBiology) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Sample: Biology", style = MaterialTheme.typography.labelSmall)
        }
        OutlinedButton(
            onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clipText = cm?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                if (!clipText.isNullOrEmpty()) {
                    onLoadSample(clipText)
                }
            },
            modifier = Modifier.weight(0.85f)
        ) {
            Icon(Icons.Filled.ContentPaste, contentDescription = "Paste clipboard notes", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Paste", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PremiumUploadActionRow(
    isPremium: Boolean,
    testTagPrefix: String,
    onOpenPlans: () -> Unit,
    onTextExtracted: (String) -> Unit,
    onUploadNote: (String) -> Unit
) {
    val tokens = LocalElvoraTokens.current
    val context = LocalContext.current
    val accent = if (tokens.isDark) SoftPinkGlow else DeepRosePink

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onUploadNote("Photo selected. Note: Offline image OCR requires a server/vision upgrade in a future release.")
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri).orEmpty()
            if (mime.startsWith("text/")) {
                val text = runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
                if (!text.isNullOrBlank()) {
                    onTextExtracted(text)
                    onUploadNote("Loaded text file successfully.")
                } else {
                    onUploadNote("Selected file was empty.")
                }
            } else {
                onUploadNote("Document selected ($mime). Plain .txt files load directly; PDF binary parsing requires an external parser.")
            }
        }
    }

    Surface(
        color = tokens.pinkBoxBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, tokens.pinkBoxBorder.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upload Photo or Study File",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                if (!isPremium) {
                    Surface(
                        color = VividMagentaPink,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable(onClick = onOpenPlans)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Premium Learner Feature",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isPremium) {
                            onOpenPlans()
                        } else {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    },
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("${testTagPrefix}_upload_photo_button")
                ) {
                    Icon(
                        imageVector = if (isPremium) Icons.Filled.AddPhotoAlternate else Icons.Filled.Lock,
                        contentDescription = if (isPremium) "Upload Photo" else "Locked Upload Photo",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Photo", style = MaterialTheme.typography.labelSmall, color = accent)
                }

                OutlinedButton(
                    onClick = {
                        if (!isPremium) {
                            onOpenPlans()
                        } else {
                            filePicker.launch(arrayOf("text/*", "application/pdf"))
                        }
                    },
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("${testTagPrefix}_upload_file_button")
                ) {
                    Icon(
                        imageVector = if (isPremium) Icons.Filled.UploadFile else Icons.Filled.Lock,
                        contentDescription = if (isPremium) "Upload File" else "Locked Upload File",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload File", style = MaterialTheme.typography.labelSmall, color = accent)
                }
            }
        }
    }
}

@Composable
private fun LockedPremiumPillButton(
    label: String,
    testTagName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalElvoraTokens.current
    val accent = if (tokens.isDark) SoftPinkGlow else DeepRosePink
    Surface(
        onClick = onClick,
        color = tokens.pinkBoxBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, tokens.pinkBoxBorder.copy(alpha = 0.7f)),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTagName)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Premium Locked",
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// A. SMART MCQs + LEARNING DIAGNOSIS
@Composable
fun SmartMcqsScreen(
    isPremium: Boolean = false,
    reducedMotion: Boolean,
    onOpenPlans: () -> Unit,
    onBack: () -> Unit,
    onRecordAttempt: (
        topic: String,
        difficulty: DifficultyLevel,
        questions: List<McqQuestion>,
        selected: Map<String, Int>,
        source: String
    ) -> DiagnosisReport,
    onSaveToLibrary: (topic: String, questions: List<McqQuestion>, source: String) -> Unit
) {
    val tokens = LocalElvoraTokens.current
    var studyText by remember { mutableStateOf("") }
    var count by remember { mutableIntStateOf(5) }
    var showCustomSlider by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf(DifficultyLevel.MEDIUM) }
    var currentTopic by remember { mutableStateOf("General Study") }
    var questions by remember { mutableStateOf<List<McqQuestion>>(emptyList()) }
    var warningMessage by remember { mutableStateOf<String?>(null) }
    val selectedMap = remember { mutableStateMapOf<String, Int>() }
    var submitted by remember { mutableStateOf(false) }
    var diagnosis by remember { mutableStateOf<DiagnosisReport?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "Smart MCQs", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ElvoraSectionCard {
                        Text(
                            text = "Generate Grounded Multiple-Choice Questions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Questions are constructed locally from your real notes — never fabricated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SampleNotesHelperRow(onLoadSample = { studyText = it })
                        PremiumUploadActionRow(
                            isPremium = isPremium,
                            testTagPrefix = "mcq",
                            onOpenPlans = onOpenPlans,
                            onTextExtracted = { studyText = it },
                            onUploadNote = { warningMessage = it }
                        )
                        OutlinedTextField(
                            value = studyText,
                            onValueChange = { studyText = it },
                            label = { Text("Paste study material, definitions, or formulas") },
                            minLines = 5,
                            maxLines = 10,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mcq_study_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Question Count", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = if (isPremium) "Premium: No Limits" else "Free limit: max 10 MCQs",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 5, 10).forEach { c ->
                                FilterChip(
                                    selected = count == c && !showCustomSlider,
                                    onClick = {
                                        showCustomSlider = false
                                        count = c
                                    },
                                    label = { Text("$c Qs") }
                                )
                            }
                        }

                        // Locked Premium Feature 1 in Smart MCQs: Unlimited / All Possible MCQs
                        if (!isPremium) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LockedPremiumPillButton(
                                    label = "Custom Count",
                                    testTagName = "mcq_locked_custom_count",
                                    onClick = onOpenPlans,
                                    modifier = Modifier.weight(1f)
                                )
                                LockedPremiumPillButton(
                                    label = "All Possible (No Limit)",
                                    testTagName = "mcq_locked_unlimited_count",
                                    onClick = onOpenPlans,
                                    modifier = Modifier.weight(1.25f)
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = showCustomSlider && count < 999,
                                    onClick = {
                                        showCustomSlider = true
                                        if (count >= 999) count = 15
                                    },
                                    label = { Text("Custom (${if (count < 999) count else 15})") }
                                )
                                FilterChip(
                                    selected = count >= 999,
                                    onClick = {
                                        showCustomSlider = false
                                        count = 999
                                    },
                                    label = { Text("All Possible (No Limit)") }
                                )
                            }
                            if (showCustomSlider && count < 999) {
                                Slider(
                                    value = count.coerceIn(1, 50).toFloat(),
                                    onValueChange = { count = it.toInt().coerceIn(1, 50) },
                                    valueRange = 1f..50f
                                )
                            }
                        }

                        Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DifficultyLevel.entries.forEach { d ->
                                FilterChip(
                                    selected = difficulty == d,
                                    onClick = { difficulty = d },
                                    label = { Text(d.label) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val effectiveCount = if (isPremium) count else count.coerceAtMost(10)
                                val res = ElvoraIntelligenceEngine.generateMcqs(studyText, effectiveCount, difficulty)
                                currentTopic = res.topic
                                questions = res.questions
                                warningMessage = res.warningMessage
                                selectedMap.clear()
                                submitted = false
                                diagnosis = null
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_mcq_button")
                        ) {
                            Text("Generate Smart MCQs", fontWeight = FontWeight.Bold)
                        }

                        if (warningMessage != null) {
                            Surface(
                                color = tokens.noticeContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = tokens.noticeColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(warningMessage!!, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                if (questions.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Topic: $currentTopic",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { onSaveToLibrary(currentTopic, questions, studyText) },
                                modifier = Modifier.testTag("save_mcq_library_button")
                            ) {
                                Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }
                        }
                        if (!isPremium && questions.size >= 10) {
                            Surface(
                                onClick = onOpenPlans,
                                color = tokens.pinkBoxBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, tokens.pinkBoxBorder.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Free tier capped at 10 MCQs per page. Unlock Unlimited MCQs →",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.WorkspacePremium,
                                        contentDescription = null,
                                        tint = VividMagentaPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(questions.size) { index ->
                        val q = questions[index]
                        val picked = selectedMap[q.id]
                        ElvoraSectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Question ${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = VividMagentaPink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${q.difficulty.label} • ${q.questionTypeTag}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(text = q.question, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)

                            q.options.forEachIndexed { optIdx, optText ->
                                val isPicked = picked == optIdx
                                val isCorrect = optIdx == q.correctIndex
                                val bgColor = when {
                                    submitted && isCorrect -> tokens.successContainer
                                    submitted && isPicked && !isCorrect -> tokens.errorContainer
                                    isPicked -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                }
                                val borderColor = when {
                                    submitted && isCorrect -> tokens.successColor
                                    submitted && isPicked && !isCorrect -> tokens.errorColor
                                    isPicked -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                }

                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, borderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !submitted) { selectedMap[q.id] = optIdx }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = optText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (submitted && isCorrect) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = "Correct", tint = tokens.successColor)
                                        } else if (submitted && isPicked) {
                                            Icon(Icons.Filled.Close, contentDescription = "Incorrect", tint = tokens.errorColor)
                                        }
                                    }
                                }
                            }

                            if (submitted) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Correction & Explanation: ${q.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        if (!submitted) {
                            Button(
                                onClick = {
                                    submitted = true
                                    diagnosis = onRecordAttempt(currentTopic, difficulty, questions, selectedMap.toMap(), studyText)
                                },
                                enabled = selectedMap.size == questions.size,
                                colors = ButtonDefaults.buttonColors(containerColor = VividMagentaPink),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_mcq_button")
                            ) {
                                Text(
                                    text = if (selectedMap.size == questions.size) "Evaluate & Run Diagnosis"
                                    else "Answer All (${selectedMap.size}/${questions.size})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            val score = questions.count { selectedMap[it.id] == it.correctIndex }
                            ElvoraSectionCard(borderColor = VividMagentaPink) {
                                AnimatedScoreCounter(
                                    score = score,
                                    total = questions.size,
                                    reducedMotion = reducedMotion,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                diagnosis?.let { rep ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = VividMagentaPink)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ELVORA Learning Diagnosis",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(text = rep.summaryMessage, style = MaterialTheme.typography.bodyMedium)

                                    if (rep.mistakeBreakdown.isNotEmpty()) {
                                        Text("Mistake Categories:", style = MaterialTheme.typography.labelLarge)
                                        rep.mistakeBreakdown.forEach { (cat, cnt) ->
                                            Text(
                                                text = "• ${cat.label} ($cnt) — ${cat.guidance}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    if (rep.repeatedCategories.isNotEmpty()) {
                                        Surface(
                                            color = tokens.errorContainer,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "Repeated Pattern Detected: ${rep.repeatedCategories.joinToString { it.label }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = tokens.errorColor,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }

                                    if (rep.followUpPracticeSet.isNotEmpty()) {
                                        Button(
                                            onClick = {
                                                questions = rep.followUpPracticeSet
                                                selectedMap.clear()
                                                submitted = false
                                                diagnosis = null
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Practice Follow-Up Set (${rep.followUpPracticeSet.size} Qs)")
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
}

// B. FLIPCARDS
@Composable
fun FlipCardsScreen(
    isPremium: Boolean = false,
    reducedMotion: Boolean,
    onOpenPlans: () -> Unit,
    onBack: () -> Unit,
    onMarkLearntLog: (topic: String, learntCount: Int) -> Unit,
    onSaveToLibrary: (topic: String, cards: List<FlipCardItem>, source: String) -> Unit
) {
    val tokens = LocalElvoraTokens.current
    var studyText by remember { mutableStateOf("") }
    var cardCount by remember { mutableIntStateOf(5) }
    var showCustomSlider by remember { mutableStateOf(false) }
    var topic by remember { mutableStateOf("General Study") }
    var cards by remember { mutableStateOf<List<FlipCardItem>>(emptyList()) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "FlipCards", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ElvoraSectionCard {
                        Text(
                            text = "Create Active-Recall FlipCards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        SampleNotesHelperRow(onLoadSample = { studyText = it })
                        PremiumUploadActionRow(
                            isPremium = isPremium,
                            testTagPrefix = "flipcard",
                            onOpenPlans = onOpenPlans,
                            onTextExtracted = { studyText = it },
                            onUploadNote = { warningMessage = it }
                        )
                        OutlinedTextField(
                            value = studyText,
                            onValueChange = { studyText = it },
                            label = { Text("Paste definitions, notes, or bullet points") },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("flipcard_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Flashcard Count", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = if (isPremium) "Premium: Custom / Unlimited" else "Free limit: max 10 cards",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tokens.isDark) SoftPinkGlow else DeepRosePink,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 5, 10).forEach { c ->
                                FilterChip(
                                    selected = cardCount == c && !showCustomSlider,
                                    onClick = {
                                        showCustomSlider = false
                                        cardCount = c
                                    },
                                    label = { Text("$c Cards") }
                                )
                            }
                        }

                        // Locked Premium Feature in FlipCards: Custom / Unlimited Card Count
                        if (!isPremium) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LockedPremiumPillButton(
                                    label = "Custom Cards",
                                    testTagName = "flipcard_locked_custom_count",
                                    onClick = onOpenPlans,
                                    modifier = Modifier.weight(1f)
                                )
                                LockedPremiumPillButton(
                                    label = "All Possible (No Limit)",
                                    testTagName = "flipcard_locked_unlimited_count",
                                    onClick = onOpenPlans,
                                    modifier = Modifier.weight(1.25f)
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = showCustomSlider && cardCount < 999,
                                    onClick = {
                                        showCustomSlider = true
                                        if (cardCount >= 999) cardCount = 15
                                    },
                                    label = { Text("Custom (${if (cardCount < 999) cardCount else 15})") }
                                )
                                FilterChip(
                                    selected = cardCount >= 999,
                                    onClick = {
                                        showCustomSlider = false
                                        cardCount = 999
                                    },
                                    label = { Text("All Possible (No Limit)") }
                                )
                            }
                            if (showCustomSlider && cardCount < 999) {
                                Slider(
                                    value = cardCount.coerceIn(1, 50).toFloat(),
                                    onValueChange = { cardCount = it.toInt().coerceIn(1, 50) },
                                    valueRange = 1f..50f
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val effectiveCount = if (isPremium) cardCount else cardCount.coerceAtMost(10)
                                val res = ElvoraIntelligenceEngine.generateFlipCards(studyText, effectiveCount)
                                topic = res.topic
                                cards = res.cards
                                currentIdx = 0
                                isFlipped = false
                                warningMessage = res.warningMessage
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_flipcards_button")
                        ) {
                            Text("Build FlipCards Deck", fontWeight = FontWeight.Bold)
                        }
                        if (warningMessage != null) {
                            Text(warningMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                if (cards.isNotEmpty()) {
                    val activeCard = cards[currentIdx]
                    val learntCount = cards.count { it.isLearnt }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Card ${currentIdx + 1} of ${cards.size} • $learntCount Learnt",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { onSaveToLibrary(topic, cards, studyText) },
                                modifier = Modifier.testTag("save_flipcards_library_button")
                            ) {
                                Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Deck")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlipCard3D(
                            frontText = activeCard.front,
                            backText = activeCard.back,
                            memoryCue = activeCard.memoryCue,
                            isFlipped = isFlipped,
                            isLearnt = activeCard.isLearnt,
                            reducedMotion = reducedMotion,
                            onFlip = { isFlipped = !isFlipped }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (currentIdx > 0) {
                                        currentIdx--
                                        isFlipped = false
                                    }
                                },
                                enabled = currentIdx > 0,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Prev")
                            }
                            Button(
                                onClick = {
                                    cards = cards.mapIndexed { idx, item ->
                                        if (idx == currentIdx) item.copy(isLearnt = !item.isLearnt) else item
                                    }
                                    val newLearnt = cards.count { it.isLearnt }
                                    onMarkLearntLog(topic, newLearnt)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VividMagentaPink),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("mark_learnt_button")
                            ) {
                                Text(if (activeCard.isLearnt) "Learnt ✓" else "Mark as Learnt", color = Color.White)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (currentIdx < cards.lastIndex) {
                                        currentIdx++
                                        isFlipped = false
                                    }
                                },
                                enabled = currentIdx < cards.lastIndex,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    }
}

// C. SMART SUMMARY
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartSummaryScreen(
    isPremium: Boolean = false,
    onOpenPlans: () -> Unit,
    onBack: () -> Unit,
    onSaveToLibrary: (SmartSummaryResult, String) -> Unit
) {
    val tokens = LocalElvoraTokens.current
    var studyText by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf<SmartSummaryResult?>(null) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "Smart Summary", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ElvoraSectionCard {
                        Text("Structured Study Summariser", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SampleNotesHelperRow(onLoadSample = { studyText = it })
                        PremiumUploadActionRow(
                            isPremium = isPremium,
                            testTagPrefix = "summary",
                            onOpenPlans = onOpenPlans,
                            onTextExtracted = { studyText = it },
                            onUploadNote = { uploadMessage = it }
                        )
                        if (uploadMessage != null) {
                            Text(uploadMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        OutlinedTextField(
                            value = studyText,
                            onValueChange = { studyText = it },
                            label = { Text("Paste study notes to organize & highlight") },
                            minLines = 5,
                            maxLines = 10,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("summary_input")
                        )
                        Button(
                            onClick = { summary = ElvoraIntelligenceEngine.generateSmartSummary(studyText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_summary_button")
                        ) {
                            Text("Generate Smart Summary", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                summary?.let { res ->
                    if (res.warningMessage != null) {
                        item {
                            ElvoraSectionCard {
                                Text(res.warningMessage, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        item {
                            ElvoraSectionCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(res.topic, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    OutlinedButton(onClick = { onSaveToLibrary(res, studyText) }) {
                                        Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save")
                                    }
                                }
                                Text("Key Points", style = MaterialTheme.typography.labelLarge, color = VividMagentaPink)
                                res.bulletPoints.forEachIndexed { i, pt ->
                                    HighlightedSentence(
                                        text = pt,
                                        highlightColor = if (i % 2 == 0) tokens.highlighterPink else tokens.highlighterBlue
                                    )
                                }

                                if (res.definitions.isNotEmpty()) {
                                    Text("Highlighted Definitions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    res.definitions.forEach { d ->
                                        Surface(color = tokens.blueBoxBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "${d.term}: ${d.definition}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }

                                if (res.formulas.isNotEmpty()) {
                                    Text("Recognisable Formulas", style = MaterialTheme.typography.labelLarge, color = VividMagentaPink)
                                    res.formulas.forEach { f ->
                                        Text(
                                            text = "• $f",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                if (res.keywords.isNotEmpty()) {
                                    Text("Extracted Keywords", style = MaterialTheme.typography.labelLarge)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        res.keywords.forEach { kw ->
                                            Surface(
                                                color = tokens.pinkBoxBg,
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(1.dp, VividMagentaPink.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = kw,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VividMagentaPink,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
    }
}

// D. QUICK ANSWERS
@Composable
fun QuickAnswersScreen(
    isPremium: Boolean = false,
    onOpenPlans: () -> Unit,
    onBack: () -> Unit,
    onSaveToLibrary: (QuickAnswerResult, String) -> Unit
) {
    val tokens = LocalElvoraTokens.current
    var question by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var answerResult by remember { mutableStateOf<QuickAnswerResult?>(null) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "Quick Answers", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ElvoraSectionCard {
                        Text(
                            text = "Transparent Evidence-Based Answers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rule-based matching strictly against your notes — displays uncertainty when information is missing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            label = { Text("Ask a question (e.g., What is inertia?)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_answer_question_input")
                        )
                        SampleNotesHelperRow(onLoadSample = { material = it })
                        PremiumUploadActionRow(
                            isPremium = isPremium,
                            testTagPrefix = "quick_answer",
                            onOpenPlans = onOpenPlans,
                            onTextExtracted = { material = it },
                            onUploadNote = { uploadMessage = it }
                        )
                        if (uploadMessage != null) {
                            Text(uploadMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        OutlinedTextField(
                            value = material,
                            onValueChange = { material = it },
                            label = { Text("Paste supporting study material") },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_answer_material_input")
                        )
                        Button(
                            onClick = { answerResult = ElvoraIntelligenceEngine.answerQuestion(question, material) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("get_quick_answer_button")
                        ) {
                            Text("Find Answer in Notes", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                answerResult?.let { res ->
                    item {
                        ElvoraSectionCard(
                            borderColor = if (res.isUncertain) tokens.noticeColor else tokens.successColor
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (res.isUncertain) "Uncertain / Insufficient Evidence" else "Matched Answer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isUncertain) tokens.noticeColor else tokens.successColor
                                )
                                if (!res.isUncertain) {
                                    OutlinedButton(onClick = {
                                        onSaveToLibrary(res, ElvoraIntelligenceEngine.identifyTopic(material))
                                    }) {
                                        Text("Save")
                                    }
                                }
                            }
                            Text(text = res.conciseAnswer, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(text = res.simpleExplanation, style = MaterialTheme.typography.bodyMedium)

                            if (res.highlightedFacts.isNotEmpty()) {
                                Text("Highlighted Supporting Points:", style = MaterialTheme.typography.labelLarge)
                                res.highlightedFacts.forEach { fact ->
                                    HighlightedSentence(text = fact, highlightColor = tokens.highlighterPink)
                                }
                            }
                            res.uncertaintyReason?.let { reason ->
                                Surface(color = tokens.noticeContainer, shape = RoundedCornerShape(10.dp)) {
                                    Text(
                                        text = "Notice: $reason",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
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
