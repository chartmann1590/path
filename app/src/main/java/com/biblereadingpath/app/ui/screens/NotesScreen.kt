package com.biblereadingpath.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TranslationIndicator(
            translationId = currentTranslation,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Your Notes",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
