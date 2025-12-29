package com.path.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartStudy: (String, Int) -> Unit,
    onNavigateToStreak: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {}
) {
    val streak by viewModel.streak.collectAsState()
    val verseOfTheDay by viewModel.verseOfTheDay.collectAsState()
    val nextChapterTitle by viewModel.nextChapter.collectAsState()
    val verseOfTheDayError by viewModel.verseOfTheDayError.collectAsState()
    val nextChapterError by viewModel.nextChapterError.collectAsState()
    
    // Refresh next chapter when screen appears (simplistic approach)
    LaunchedEffect(Unit) {
        viewModel.loadNextChapter()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Path",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToStreak)
                .semantics { contentDescription = "View streak and progress" },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "$streak Day Streak", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Tap to see your progress", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View streak"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Next Study", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                if (nextChapterError != null) {
                    Text(
                        text = "Error loading next chapter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.loadNextChapter() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry")
                    }
                } else {
                    Text(text = nextChapterTitle, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onStartStudy(viewModel.nextBookStr, viewModel.nextChapterInt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Start reading ${nextChapterTitle}" }
                    ) {
                        Text("Start Reading")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onNavigateToDownloads)
                    .semantics { contentDescription = "Navigate to downloads" }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onNavigateToStreak)
                    .semantics { contentDescription = "Navigate to progress" }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Progress",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Verse of the Day", style = MaterialTheme.typography.labelLarge)
                    if (verseOfTheDay != null) {
                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                val shareText = "\"${verseOfTheDay!!.text}\" - ${verseOfTheDay!!.book} ${verseOfTheDay!!.chapter}:${verseOfTheDay!!.number}"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share verse")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.semantics { contentDescription = "Share verse of the day" }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (verseOfTheDayError != null) {
                    Text(
                        text = verseOfTheDayError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.loadVerseOfTheDay() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry")
                    }
                } else if (verseOfTheDay != null) {
                    Text(
                        text = "\"${verseOfTheDay!!.text}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Text(
                        text = "${verseOfTheDay!!.book} ${verseOfTheDay!!.chapter}:${verseOfTheDay!!.number}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.End)
                    )
                } else {
                    Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
