package com.path.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Stop

import com.path.app.ui.components.AdMobInterstitialManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    book: String,
    chapterNumber: Int,
    viewModel: ReaderViewModel,
    onBack: () -> Unit,
    onNextChapter: (String, Int) -> Unit,
    interstitialAdManager: AdMobInterstitialManager? = null
) {
    LaunchedEffect(book, chapterNumber) {
        viewModel.loadChapter(book, chapterNumber)
    }
    
    // Stop TTS when navigating away
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTts()
        }
    }

    var showNoteDialog by remember { mutableStateOf<Int?>(null) }
    var noteContent by remember { mutableStateOf("") }
    
    // AI State
    val aiExplanation = viewModel.aiExplanation
    val isGeneratingAi = viewModel.isGeneratingAi
    val isPlayingAudio = viewModel.isPlayingAudio
    val completionMessage = viewModel.completionMessage
    val isGeneratingCompletionMessage = viewModel.isGeneratingCompletionMessage
    val chapterError = viewModel.chapterError
    val currentWordPosition = viewModel.currentWordPosition
    
    if (aiExplanation != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearAiExplanation() },
            title = { Text("AI Insight") },
            text = { 
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text(aiExplanation) 
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearAiExplanation() },
                    modifier = Modifier.semantics { contentDescription = "Close AI insight dialog" }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showNoteDialog != null) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = null },
            title = { Text("Verse ${showNoteDialog}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Your reflection") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (viewModel.aiEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isGeneratingAi) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text("Generating insight...", style = MaterialTheme.typography.bodySmall)
                        } else {
                            OutlinedButton(
                                onClick = { 
                                    // Find verse text
                                    val verseText = viewModel.chapter?.verses?.find { it.number == showNoteDialog }?.text ?: ""
                                    viewModel.explainVerse(verseText) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { contentDescription = "Get AI explanation for verse ${showNoteDialog}" }
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "AI explanation", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Explain with AI")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveNote(showNoteDialog!!, noteContent)
                        showNoteDialog = null
                        noteContent = ""
                    },
                    modifier = Modifier.semantics { contentDescription = "Save note for verse ${showNoteDialog}" }
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNoteDialog = null },
                    modifier = Modifier.semantics { contentDescription = "Cancel note dialog" }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show completion message when available
    LaunchedEffect(completionMessage) {
        completionMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearCompletionMessage()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("$book $chapterNumber") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleAudio() }) {
                        Icon(
                            if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow, 
                            contentDescription = if (isPlayingAudio) "Stop Audio" else "Play Audio"
                        )
                    }
                    IconButton(onClick = { onNextChapter(book, chapterNumber + 1) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!viewModel.isCompleted) {
                ExtendedFloatingActionButton(
                    onClick = { 
                        viewModel.markAsCompleted()
                        // Try to show ad after marking chapter as complete
                        interstitialAdManager?.tryShowAd()
                        onBack()
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Mark chapter as complete") },
                    text = { Text("Done") },
                    modifier = Modifier.semantics { contentDescription = "Mark chapter as complete" }
                )
            }
        }
    ) { padding ->
        val chapter = viewModel.chapter
        if (chapter != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(chapter.verses) { verse ->
                    var isFavorite by remember { mutableStateOf(false) }

                    LaunchedEffect(verse) {
                        isFavorite = viewModel.isVerseFavorite(verse)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showNoteDialog = verse.number }
                                .semantics { contentDescription = "Verse ${verse.number}: ${verse.text.take(50)}${if (verse.text.length > 50) "..." else ""}. Tap to add note" }
                        ) {
                            Text(
                                text = "${verse.number} ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            
                            // Get colors outside remember block
                            val primaryContainer = MaterialTheme.colorScheme.primaryContainer
                            val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
                            
                            // Build annotated text with highlighting for current word
                            val annotatedText = remember(verse.text, currentWordPosition, verse.number, primaryContainer, onPrimaryContainer) {
                                buildAnnotatedString {
                                    val words = verse.text.split(Regex("\\s+")).filter { it.isNotBlank() }
                                    val isCurrentVerse = currentWordPosition?.verseNumber == verse.number
                                    val currentWordIndex = currentWordPosition?.wordIndexInVerse
                                    
                                    words.forEachIndexed { index, word ->
                                        val isHighlighted = isCurrentVerse && index == currentWordIndex
                                        
                                        if (isHighlighted) {
                                            withStyle(
                                                style = SpanStyle(
                                                    background = primaryContainer,
                                                    color = onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) {
                                                append(word)
                                            }
                                        } else {
                                            append(word)
                                        }
                                        
                                        // Add space after word (except last)
                                        if (index < words.size - 1) {
                                            append(" ")
                                        }
                                    }
                                }
                            }
                            
                            Text(
                                text = annotatedText,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 28.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(verse)
                                isFavorite = !isFavorite
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                    Button(
                        onClick = { 
                            viewModel.markAsCompleted()
                            // Try to show ad after completing chapter
                            interstitialAdManager?.tryShowAd()
                            onNextChapter(book, chapterNumber + 1) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Finish chapter and go to next chapter" }
                    ) {
                        Text("Finish & Next Chapter")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else if (chapterError != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = chapterError!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadChapter(book, chapterNumber) }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
