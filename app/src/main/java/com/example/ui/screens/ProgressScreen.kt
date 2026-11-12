package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ActivityLogEntity
import com.example.data.local.MistakeRecordEntity
import com.example.data.local.QuizAttemptEntity
import com.example.ui.components.ElvoraSectionCard
import com.example.ui.theme.DeepRosePink
import com.example.ui.theme.MidOceanBlue
import com.example.ui.theme.VividMagentaPink
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    attempts: List<QuizAttemptEntity>,
    mistakes: List<MistakeRecordEntity>,
    activities: List<ActivityLogEntity>
) {
    val totalActivities = activities.size
    val totalScore = attempts.sumOf { it.score }
    val totalMax = attempts.sumOf { it.totalMarks }
    val overallAccuracy = if (totalMax > 0) (totalScore * 100) / totalMax else 0
    val learntCards = activities.sumOf { it.cardsLearntCount }

    // Genuine day streak calculation from activity timestamps
    val streakDays = remember(activities) {
        if (activities.isEmpty()) 0
        else {
            val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
            val activeDays = activities.map { dayFormat.format(Date(it.createdAt)) }.toSet()
            val cal = Calendar.getInstance()
            var streak = 0
            while (activeDays.contains(dayFormat.format(cal.time))) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            streak.coerceAtLeast(if (activeDays.isNotEmpty()) 1 else 0)
        }
    }

    // Genuine last-7-days activity counts for chart
    val weeklyCounts = remember(activities) {
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val labelFormat = SimpleDateFormat("EEE", Locale.US)
        val grouped = activities.groupingBy { dayFormat.format(Date(it.createdAt)) }.eachCount()
        (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val key = dayFormat.format(cal.time)
            labelFormat.format(cal.time) to (grouped[key] ?: 0)
        }
    }

    // Subject/Topic performance breakdown
    val topicPerformance = remember(attempts) {
        attempts.groupBy { it.topic }.map { (topic, list) ->
            val s = list.sumOf { it.score }
            val m = list.sumOf { it.totalMarks }.coerceAtLeast(1)
            Triple(topic, list.size, (s * 100) / m)
        }.sortedByDescending { it.second }
    }

    val weakTopics = remember(mistakes) {
        mistakes.groupingBy { it.topic }.eachCount().entries.sortedByDescending { it.value }
    }

    val improvementDelta = remember(attempts) {
        if (attempts.size < 2) null
        else {
            val sorted = attempts.sortedBy { it.createdAt }
            val firstPct = (sorted.first().score * 100) / sorted.first().totalMarks.coerceAtLeast(1)
            val lastPct = (sorted.last().score * 100) / sorted.last().totalMarks.coerceAtLeast(1)
            lastPct - firstPct
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Progress & Learning Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "100% real performance metrics computed from your completed sessions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (totalActivities == 0 && attempts.isEmpty()) {
                item {
                    ElvoraSectionCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Insights,
                                contentDescription = null,
                                tint = VividMagentaPink,
                                modifier = Modifier.height(48.dp)
                            )
                            Text("No Learning History Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "ELVORA never invents placeholder scores. Complete a Smart MCQs quiz or mark FlipCards as learnt to unlock your real analytics here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Core Metrics Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricTile("Activities", "$totalActivities", MidOceanBlue, Modifier.weight(1f))
                        MetricTile("Accuracy", if (attempts.isEmpty()) "—" else "$overallAccuracy%", VividMagentaPink, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricTile("Streak", "$streakDays day(s)", DeepRosePink, Modifier.weight(1f))
                        MetricTile("Cards Learnt", "$learntCards", MidOceanBlue, Modifier.weight(1f))
                    }
                }

                // Weekly Progress Bar Chart (Canvas)
                item {
                    ElvoraSectionCard {
                        Text("Weekly Activity (Last 7 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val maxVal = (weeklyCounts.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(top = 8.dp)
                        ) {
                            val barSpace = size.width / weeklyCounts.size
                            val barWidth = barSpace * 0.48f
                            weeklyCounts.forEachIndexed { idx, (_, cnt) ->
                                val ratio = cnt.toFloat() / maxVal.toFloat()
                                val barH = (size.height * 0.88f) * ratio
                                val x = idx * barSpace + (barSpace - barWidth) / 2f
                                val y = size.height - barH
                                drawRoundRect(
                                    color = if (idx == weeklyCounts.lastIndex) VividMagentaPink else MidOceanBlue,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barH.coerceAtLeast(6.dp.toPx())),
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            weeklyCounts.forEach { (dayLabel, cnt) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(cnt.toString(), style = MaterialTheme.typography.labelSmall, color = VividMagentaPink)
                                    Text(dayLabel, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Improvement Over Time
                if (improvementDelta != null) {
                    item {
                        ElvoraSectionCard(borderColor = VividMagentaPink) {
                            Text("Improvement Over Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val trendMsg = when {
                                improvementDelta > 0 -> "+$improvementDelta% accuracy improvement from your first quiz to your latest quiz!"
                                improvementDelta < 0 -> "${improvementDelta}% change compared to your first quiz — review weak topics below."
                                else -> "Consistent accuracy across quizzes."
                            }
                            Text(trendMsg, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Subject / Topic Performance
                if (topicPerformance.isNotEmpty()) {
                    item {
                        ElvoraSectionCard {
                            Text("Subject & Topic Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            topicPerformance.forEach { (topic, count, pct) ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$topic ($count quiz)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text("$pct%", style = MaterialTheme.typography.labelMedium, color = VividMagentaPink)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                                        color = VividMagentaPink,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(50))
                                    )
                                }
                            }
                        }
                    }
                }

                // Weak Topics from Diagnosis
                if (weakTopics.isNotEmpty()) {
                    item {
                        ElvoraSectionCard {
                            Text("Identified Weak Topics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            weakTopics.forEach { (topic, missCount) ->
                                Text("• $topic ($missCount logged mistake(s))", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Previous Quiz Attempts
                if (attempts.isNotEmpty()) {
                    item {
                        ElvoraSectionCard {
                            Text("Previous Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            attempts.take(6).forEach { att ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "${att.topic} • ${att.score}/${att.totalMarks} (${att.difficulty})",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = att.diagnosisSummary,
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
private fun MetricTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
