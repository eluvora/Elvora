package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FeedbackEntity
import com.example.data.local.UserEntity
import com.example.data.prefs.ThemePreference
import com.example.data.prefs.UserSettingsState
import com.example.ui.components.ElvoraSectionCard
import com.example.ui.theme.DeepRosePink
import com.example.ui.theme.LocalElvoraTokens
import com.example.ui.theme.MidOceanBlue
import com.example.ui.theme.SoftPinkGlow
import com.example.ui.theme.SoftSkyBlue
import com.example.ui.theme.VividMagentaPink

// FEEDBACK PAGE
@Composable
fun FeedbackScreen(
    submissions: List<FeedbackEntity>,
    onSubmitFeedback: (feature: String, rating: Int, liked: String, improvements: String, onDone: () -> Unit) -> Unit,
    onMaybeLater: () -> Unit
) {
    val features = listOf("General", "Smart MCQs", "FlipCards", "Smart Summary", "Quick Answers", "Welcome Animation", "Premium Previews")
    var selectedFeature by remember { mutableStateOf("General") }
    var rating by remember { mutableIntStateOf(5) }
    var liked by remember { mutableStateOf("") }
    var improvements by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Student Feedback", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Optional feedback helps improve ELVORA. Stored locally in Room until cloud database sync is configured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ElvoraSectionCard {
                    Text("Relevant Feature", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        features.take(3).forEach { f ->
                            FilterChip(selected = selectedFeature == f, onClick = { selectedFeature = f }, label = { Text(f, style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        features.drop(3).take(4).forEach { f ->
                            FilterChip(selected = selectedFeature == f, onClick = { selectedFeature = f }, label = { Text(f, style = MaterialTheme.typography.labelSmall) })
                        }
                    }

                    Text("Rate your experience (1 to 5 stars)", style = MaterialTheme.typography.labelLarge)
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) VividMagentaPink else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = liked,
                        onValueChange = { liked = it; validationError = null },
                        label = { Text("What did you like?") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_liked_input")
                    )
                    OutlinedTextField(
                        value = improvements,
                        onValueChange = { improvements = it; validationError = null },
                        label = { Text("What can we improve?") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_improve_input")
                    )

                    if (validationError != null) {
                        Text(validationError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onMaybeLater,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Maybe Later")
                        }
                        Button(
                            onClick = {
                                if (liked.isBlank() && improvements.isBlank()) {
                                    validationError = "Please share at least one note on what you liked or what we can improve."
                                    return@Button
                                }
                                onSubmitFeedback(selectedFeature, rating, liked, improvements) {
                                    liked = ""
                                    improvements = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VividMagentaPink),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("submit_feedback_button")
                        ) {
                            Text("Submit Feedback", color = Color.White)
                        }
                    }
                }
            }

            item {
                Text("Previous Submissions (${submissions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (submissions.isEmpty()) {
                item {
                    ElvoraSectionCard {
                        Text("No feedback submitted yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(submissions, key = { it.id }) { fb ->
                    ElvoraSectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fb.feature, style = MaterialTheme.typography.labelLarge, color = VividMagentaPink)
                            Text("${fb.rating} / 5 ★", style = MaterialTheme.typography.labelLarge, color = VividMagentaPink)
                        }
                        if (fb.liked.isNotBlank()) Text("Liked: ${fb.liked}", style = MaterialTheme.typography.bodySmall)
                        if (fb.improvements.isNotBlank()) Text("Improve: ${fb.improvements}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// PROFILE PAGE
@Composable
fun ProfileScreen(
    user: UserEntity?,
    totalActivities: Int,
    totalSaved: Int,
    onUpdateName: (String) -> Unit,
    onOpenPlans: () -> Unit,
    onBack: () -> Unit
) {
    var editingName by remember(user) { mutableStateOf(user?.name ?: "") }
    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "Student Profile", onBack = onBack)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_student_avatar),
                                contentDescription = "Student profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(user?.name ?: "ELVORA Learner", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(user?.email ?: "demo@elvora.local", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Account Mode: Local Demo Mode", style = MaterialTheme.typography.labelSmall, color = VividMagentaPink)
                            }
                        }
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { if (editingName.isNotBlank()) onUpdateName(editingName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Profile Name")
                        }
                    }
                }

                item {
                    ElvoraSectionCard {
                        Text("Learning Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("• Total Completed Activities: $totalActivities", style = MaterialTheme.typography.bodySmall)
                        Text("• Saved Library Items: $totalSaved", style = MaterialTheme.typography.bodySmall)
                    }
                }

                item {
                    ElvoraSectionCard(borderColor = VividMagentaPink) {
                        Text("Subscription Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Current Plan: Free Tier (₹0)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "No active paid subscription. Premium features remain locked until server-verified subscription activation. Payment & plan contact: Instagram @elvora.axiom",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = onOpenPlans, modifier = Modifier.fillMaxWidth()) {
                            Text("View Subscription Plans")
                        }
                    }
                }
            }
        }
    }
}

// SETTINGS PAGE
@Composable
fun SettingsScreen(
    settings: UserSettingsState,
    onSetTheme: (ThemePreference) -> Unit,
    onToggleMusic: () -> Unit,
    onSetVolume: (Float) -> Unit,
    onSetNotifications: (Boolean) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onClearData: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "Settings", onBack = onBack)
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
                        Text("Theme Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Blue, Purple & Magenta-Pink Academic Palette", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreference.entries.forEach { pref ->
                                FilterChip(
                                    selected = settings.themePreference == pref,
                                    onClick = { onSetTheme(pref) },
                                    label = { Text(pref.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                }

                item {
                    ElvoraSectionCard {
                        Text("Retro Indie Lo-Fi Music", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("1960s Dreamy Guitar, Bass & Vintage Drums", style = MaterialTheme.typography.bodyMedium)
                                Text("Starts off by default — toggle on anytime.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = settings.musicEnabled, onCheckedChange = { onToggleMusic() })
                        }
                        if (settings.musicEnabled) {
                            Slider(
                                value = settings.musicVolume,
                                onValueChange = onSetVolume,
                                valueRange = 0f..1f
                            )
                        }
                    }
                }

                item {
                    ElvoraSectionCard {
                        Text("Accessibility & Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reduced Motion", style = MaterialTheme.typography.bodyMedium)
                                Text("Minimizes card flips, score counters & background motion.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = settings.reducedMotion, onCheckedChange = onSetReducedMotion)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Study Reminder Preference", style = MaterialTheme.typography.bodyMedium)
                                Text("Local session preference toggle.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = settings.notificationsEnabled, onCheckedChange = onSetNotifications)
                        }
                    }
                }

                item {
                    ElvoraSectionCard {
                        Text("Account Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = onClearData, modifier = Modifier.fillMaxWidth()) {
                            Text("Clear Local Study History & Library")
                        }
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_logout_button")
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Out")
                        }
                    }
                }
            }
        }
    }
}

// HELP & SUPPORT PAGE
@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    val tokens = LocalElvoraTokens.current
    val uriHandler = LocalUriHandler.current
    val pinkAccent = if (tokens.isDark) SoftPinkGlow else DeepRosePink
    val blueAccent = if (tokens.isDark) SoftSkyBlue else MidOceanBlue

    Column(modifier = Modifier.fillMaxSize()) {
        ToolTopBar(title = "Help & Support", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ElvoraSectionCard(borderColor = tokens.blueBoxBorder.copy(alpha = 0.5f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SupportAgent,
                                contentDescription = null,
                                tint = pinkAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Contact & Query Support",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "For any query, feedback, or plan assistance, reach out directly using Instagram or WhatsApp below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Instagram Contact Card
                item {
                    Surface(
                        color = tokens.pinkBoxBg,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.2.dp, tokens.pinkBoxBorder.copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AlternateEmail,
                                    contentDescription = "Instagram Contact",
                                    tint = pinkAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Instagram",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = pinkAccent
                                )
                            }
                            Text(
                                text = "@elvora.axiom",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Send a direct message for app queries or subscription support.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = {
                                    runCatching { uriHandler.openUri("https://instagram.com/elvora.axiom") }
                                },
                                border = BorderStroke(1.dp, pinkAccent.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("help_instagram_button")
                            ) {
                                Text("Message @elvora.axiom on Instagram", color = pinkAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // WhatsApp Contact Card (Do Not Call)
                item {
                    Surface(
                        color = tokens.blueBoxBg,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.2.dp, tokens.blueBoxBorder.copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Chat,
                                        contentDescription = "WhatsApp Contact",
                                        tint = blueAccent
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "WhatsApp",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = blueAccent
                                    )
                                }
                                Surface(
                                    color = VividMagentaPink,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhoneDisabled,
                                            contentDescription = "Do Not Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "DO NOT CALL",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "7999940284",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Surface(
                                color = tokens.pinkBoxBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, tokens.pinkBoxBorder.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Do not call — WhatsApp messages only for queries.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = pinkAccent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    runCatching { uriHandler.openUri("https://wa.me/917999940284") }
                                },
                                border = BorderStroke(1.dp, blueAccent.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("help_whatsapp_button")
                            ) {
                                Text("Message on WhatsApp (7999940284)", color = blueAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
