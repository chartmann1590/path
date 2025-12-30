package com.biblereadingpath.app.ui.screens

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
import androidx.compose.material.icons.filled.Help
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
import androidx.compose.ui.unit.size

import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.MenuBook

import com.biblereadingpath.app.ui.components.AdMobInterstitialManager
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.ui.components.TutorialOverlay
import com.biblereadingpath.app.ui.components.TutorialTarget
import com.biblereadingpath.app.ui.components.TutorialStep
import com.biblereadingpath.app.ui.components.TranslationIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.LayoutCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    book: String,
    chapterNumber: Int,
    viewModel: ReaderViewModel,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onNextChapter: (String, Int) -> Unit,
    onSwitchBook: (String, Int) -> Unit = { _, _ -> },
    onTakeQuiz: (String, Int) -> Unit = { _, _ -> },
    interstitialAdManager: AdMobInterstitialManager? = null
) {
    val tutorialCompleted by userPreferences.tutorialCompleted.collectAsState(initial = false)
    var showTutorial by remember { mutableStateOf(false) }
    var currentTutorialStep by remember { mutableStateOf(0) }
    var targetCoordinates by remember { mutableStateOf<Map<String, LayoutCoordinates>>(emptyMap()) }
    
    val tutorialSteps = listOf(
        TutorialStep(
            targetId = "verse_note",
            title = "Add Notes",
            description = "Tap any verse to add a note or reflection. Your notes are saved locally and linked to the specific verse.",
            anchor = Alignment.BottomCenter
        ),
        TutorialStep(
            targetId = "favorite_button",
            title = "Favorite Verses",
            description = "Tap the star icon to save verses to your favorites. Access them anytime from the Favorites tab.",
            anchor = Alignment.CenterEnd
        ),
        TutorialStep(
            targetId = "audio_button",
            title = "Audio Playback",
            description = "Tap the play icon to listen to the chapter being read aloud. Perfect for hands-free study!",
            anchor = Alignment.BottomCenter
        ),
        TutorialStep(
            targetId = "next_chapter_button",
            title = "Navigate Chapters",
            description = "Use the arrow buttons to move between chapters. You can also switch books using the book icon.",
            anchor = Alignment.BottomCenter
        ),
        TutorialStep(
            targetId = "complete_button",
            title = "Mark Complete",
            description = "Tap 'Done' when you finish reading to update your streak and track your progress.",
            anchor = Alignment.TopCenter
        )
    )
    
    // Check if tutorial should be shown (only if not completed and on first visit to reader)
    LaunchedEffect(tutorialCompleted) {
        if (!tutorialCompleted && !showTutorial) {
            kotlinx.coroutines.delay(1000) // Wait for UI to load
            showTutorial = true
        }
    }
    
    fun handleTutorialNext() {
        if (currentTutorialStep < tutorialSteps.size - 1) {
            currentTutorialStep++
        } else {
            showTutorial = false
            CoroutineScope(Dispatchers.IO).launch {
                userPreferences.setTutorialCompleted(true)
            }
        }
    }
    
    fun handleTutorialSkip() {
        showTutorial = false
        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.setTutorialCompleted(true)
        }
    }
    LaunchedEffect(book, chapterNumber) {
        viewModel.loadChapter(book, chapterNumber)
        viewModel.loadDownloadedBooks()
    }

    // Stop TTS when navigating away
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTts()
        }
    }

    var showNoteDialog by remember { mutableStateOf<Int?>(null) }
    var noteContent by remember { mutableStateOf("") }
    var showBookSelector by remember { mutableStateOf(false) }
    var showQuizPromptDialog by remember { mutableStateOf(false) }
    val currentTranslation by viewModel.currentTranslation.collectAsState()
    
    // AI State
    val aiExplanation = viewModel.aiExplanation
    val isGeneratingAi = viewModel.isGeneratingAi
    val isPlayingAudio = viewModel.isPlayingAudio
    val completionMessage = viewModel.completionMessage
    val isGeneratingCompletionMessage = viewModel.isGeneratingCompletionMessage
    val chapterError = viewModel.chapterError
    val currentWordPosition = viewModel.currentWordPosition
    
    // Quiz prompt dialog
    if (showQuizPromptDialog) {
        AlertDialog(
            onDismissRequest = { showQuizPromptDialog = false },
            title = { Text("Take Quiz?") },
            text = { 
                Text("Would you like to take a quiz about this chapter before finishing?")
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showQuizPromptDialog = false
                        onTakeQuiz(book, chapterNumber)
                    }
                ) {
                    Text("Yes, Take Quiz")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showQuizPromptDialog = false
                        viewModel.markAsCompleted()
                        // Try to show ad after marking chapter as complete
                        interstitialAdManager?.tryShowAd()
                        onBack()
                    }
                ) {
                    Text("No, Just Finish")
                }
            }
        )
    }
    
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

    // Book selector dialog
    if (showBookSelector) {
        AlertDialog(
            onDismissRequest = { showBookSelector = false },
            title = { Text("Switch Book") },
            text = {
                LazyColumn {
                    items(viewModel.downloadedBooks) { downloadedBook ->
                        TextButton(
                            onClick = {
                                onSwitchBook(downloadedBook.name, 1)
                                showBookSelector = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = downloadedBook.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (downloadedBook.name == book) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "${downloadedBook.maxChapter} chapters",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (downloadedBook.name == book) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Current book",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookSelector = false }) {
                    Text("Close")
                }
            }
        )
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
                    IconButton(
                        onClick = { showBookSelector = true },
                        enabled = viewModel.downloadedBooks.isNotEmpty()
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Switch Book")
                    }
                    TutorialTarget(
                        stepId = "audio_button",
                        onPositioned = { id, coords ->
                            if (id.isNotEmpty()) {
                                targetCoordinates = targetCoordinates + (id to coords)
                            }
                        }
                    ) {
                        IconButton(onClick = { viewModel.toggleAudio() }) {
                            Icon(
                                if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingAudio) "Stop Audio" else "Play Audio"
                            )
                        }
                    }
                    TutorialTarget(
                        stepId = "next_chapter_button",
                        onPositioned = { id, coords ->
                            if (id.isNotEmpty()) {
                                targetCoordinates = targetCoordinates + (id to coords)
                            }
                        }
                    ) {
                        IconButton(onClick = { onNextChapter(book, chapterNumber + 1) }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!viewModel.isCompleted) {
                TutorialTarget(
                    stepId = "complete_button",
                    onPositioned = { id, coords ->
                        targetCoordinates = targetCoordinates + (id to coords)
                    }
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { 
                            if (viewModel.aiEnabled) {
                                showQuizPromptDialog = true
                            } else {
                                viewModel.markAsCompleted()
                                // Try to show ad after marking chapter as complete
                                interstitialAdManager?.tryShowAd()
                                onBack()
                            }
                        },
                        icon = { Icon(Icons.Default.Check, contentDescription = "Mark chapter as complete") },
                        text = { Text("Done") },
                        modifier = Modifier.semantics { contentDescription = "Mark chapter as complete" }
                    )
                }
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
                item {
                    TranslationIndicator(
                        translationId = currentTranslation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                items(chapter.verses) { verse ->
                    var isFavorite by remember { mutableStateOf(false) }

                    LaunchedEffect(verse) {
                        isFavorite = viewModel.isVerseFavorite(verse)
                    }

                    TutorialTarget(
                        stepId = if (verse.number == 1) "verse_note" else "",
                        onPositioned = { id, coords ->
                            if (id.isNotEmpty()) {
                                targetCoordinates = targetCoordinates + (id to coords)
                            }
                        }
                    ) {
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
                        TutorialTarget(
                            stepId = if (verse.number == 1) "favorite_button" else "",
                            onPositioned = { id, coords ->
                                if (id.isNotEmpty()) {
                                    targetCoordinates = targetCoordinates + (id to coords)
                                }
                            }
                        ) {
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
                    if (viewModel.aiEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onTakeQuiz(book, chapterNumber) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Take quiz for $book $chapterNumber" }
                        ) {
                            Icon(Icons.Default.Help, contentDescription = "Quiz icon", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Quiz")
                        }
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
        
        // Tutorial overlay
        if (showTutorial && currentTutorialStep < tutorialSteps.size) {
            TutorialOverlay(
                steps = tutorialSteps,
                currentStepIndex = currentTutorialStep,
                onNext = { handleTutorialNext() },
                onSkip = { handleTutorialSkip() },
                targetCoordinates = targetCoordinates
            )
        }
    }
}
