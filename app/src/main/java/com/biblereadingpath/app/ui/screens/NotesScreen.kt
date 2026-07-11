package com.biblereadingpath.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.ui.components.TranslationIndicator
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    userPreferences: UserPreferences
) {
    val notes by viewModel.notes.collectAsState()
    val currentTranslation by userPreferences.translation.collectAsState(initial = "web")
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val text = buildString {
                    appendLine("Path - Bible Study Notes")
                    appendLine("Exported: ${sdf.format(Date())}")
                    appendLine("=" .repeat(40))
                    appendLine()
                    notes.forEach { note ->
                        appendLine("${note.bookName} ${note.chapter}:${note.verse}")
                        appendLine("Date: ${sdf.format(Date(note.timestamp))}")
                        appendLine(note.content)
                        appendLine("-".repeat(40))
                        appendLine()
                    }
                }
                outputStream.write(text.toByteArray())
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TranslationIndicator(
            translationId = currentTranslation,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Notes",
                style = MaterialTheme.typography.headlineMedium
            )
            if (notes.isNotEmpty()) {
                Row {
                    IconButton(
                        onClick = {
                            val text = buildString {
                                notes.forEach { note ->
                                    append("\"${note.content}\" - ${note.bookName} ${note.chapter}:${note.verse}")
                                    appendLine()
                                }
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text.toString())
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share notes"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share notes")
                    }
                    TextButton(
                        onClick = {
                            exportLauncher.launch("path-notes-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.txt")
                        }
                    ) {
                        Text("Export")
                    }
                }
            }
        }

        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notes yet. Start reading and add some!", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "${note.bookName} ${note.chapter}:${note.verse}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.timestamp)),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = note.content, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
