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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LibraryItemEntity
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.ElvoraSectionCard
import com.example.ui.theme.VividMagentaPink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val libraryCategories = listOf(
    "ALL" to "All",
    "MCQS" to "MCQs",
    "FLIPCARDS" to "FlipCards",
    "SMART_SUMMARY" to "Smart Summaries",
    "QUICK_ANSWERS" to "Quick Answers",
    "SMART_NOTES" to "SmartNotes",
    "CUSTOM_QUIZZES" to "Custom Quizzes",
    "REVISION_SHEETS" to "Revision Sheets",
    "STUDYCAST" to "StudyCast"
)

@Composable
fun LibraryScreen(
    items: List<LibraryItemEntity>,
    onUpdateItem: (LibraryItemEntity) -> Unit,
    onDeleteItem: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var newestFirst by remember { mutableStateOf(true) }
    var openedItem by remember { mutableStateOf<LibraryItemEntity?>(null) }
    var editingItem by remember { mutableStateOf<LibraryItemEntity?>(null) }

    val filtered = remember(items, searchQuery, selectedCategory, newestFirst) {
        val base = items.filter { item ->
            val catMatch = selectedCategory == "ALL" || item.category == selectedCategory
            val q = searchQuery.trim()
            val textMatch = q.isEmpty() ||
                item.title.contains(q, ignoreCase = true) ||
                item.topic.contains(q, ignoreCase = true) ||
                item.sourcePreview.contains(q, ignoreCase = true)
            catMatch && textMatch
        }
        if (newestFirst) base.sortedByDescending { it.updatedAt } else base.sortedBy { it.updatedAt }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Personal Learning Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                DemoModeBanner(
                    text = "Stored in local device storage (Room). Content remains accessible across sessions on this device; not cloud-synced."
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search saved notes, topics...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_search_input")
                    )
                    OutlinedButton(onClick = { newestFirst = !newestFirst }) {
                        Icon(Icons.Filled.Sort, contentDescription = "Sort by date", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (newestFirst) "Newest" else "Oldest", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(libraryCategories) { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    ElvoraSectionCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_library_empty),
                                contentDescription = "Empty library illustration",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(145.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Text(
                                text = if (items.isEmpty()) "Your Library is Empty" else "No Matching Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (items.isEmpty())
                                    "Save Smart MCQs, FlipCards, Smart Summaries, or Quick Answers to build your personal revision archive."
                                else "Try clearing your search filter or choosing All categories.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { item ->
                    ElvoraSectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    color = VividMagentaPink.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = libraryCategories.firstOrNull { it.first == item.category }?.second ?: item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VividMagentaPink,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Topic: ${item.topic} • Updated ${dateFormat.format(Date(item.updatedAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.sourcePreview.isNotBlank()) {
                                    Text(
                                        text = item.sourcePreview,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { openedItem = item }) { Text("Open") }
                            IconButton(onClick = { editingItem = item }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit item")
                            }
                            IconButton(onClick = { onDeleteItem(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete item", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    openedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { openedItem = null },
            title = { Text(item.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Topic: ${item.topic}", style = MaterialTheme.typography.labelLarge, color = VividMagentaPink)
                    Text(item.sourcePreview, style = MaterialTheme.typography.bodySmall)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = item.contentJson.take(900),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { openedItem = null }) { Text("Close") }
            }
        )
    }

    editingItem?.let { item ->
        var newTitle by remember(item) { mutableStateOf(item.title) }
        var newTopic by remember(item) { mutableStateOf(item.topic) }
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit Library Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTopic,
                        onValueChange = { newTopic = it },
                        label = { Text("Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onUpdateItem(item.copy(title = newTitle.trim(), topic = newTopic.trim()))
                            editingItem = null
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("Cancel") }
            }
        )
    }
}
