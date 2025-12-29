package com.path.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.path.app.analytics.FirebaseManager
import com.path.app.data.local.entity.FavoriteEntity
import com.path.app.data.local.entity.NoteEntity
import com.path.app.data.local.entity.ProgressEntity
import com.path.app.data.preferences.UserPreferences
import com.path.app.data.repository.BibleRepository
import com.path.app.data.repository.OllamaRepository
import com.path.app.data.repository.PathRepository
import com.path.app.domain.model.Chapter
import com.path.app.domain.model.Verse
import com.path.app.ui.components.TtsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// Data class to track which verse and word is currently being spoken
data class CurrentWordPosition(
    val verseNumber: Int,
    val wordIndexInVerse: Int
)

class ReaderViewModel(
    private val bibleRepository: BibleRepository,
    private val pathRepository: PathRepository,
    private val userPreferences: UserPreferences,
    private val firebaseManager: FirebaseManager,
    context: Context
) : ViewModel() {
    private val ollamaRepository = OllamaRepository(userPreferences)
    private val ttsManager = TtsManager(context)

    var chapter by mutableStateOf<Chapter?>(null)
        private set
    
    var chapterError by mutableStateOf<String?>(null)
        private set

    var isPlayingAudio by mutableStateOf(false)
        private set

    var isCompleted by mutableStateOf(false)
        private set

    var aiEnabled by mutableStateOf(false)
        private set

    var aiExplanation by mutableStateOf<String?>(null)
        private set

    var isGeneratingAi by mutableStateOf(false)
        private set
    
    var completionMessage by mutableStateOf<String?>(null)
        private set
    
    var isGeneratingCompletionMessage by mutableStateOf(false)
        private set

    // Word-by-word highlighting state
    var currentWordPosition by mutableStateOf<CurrentWordPosition?>(null)
        private set

    // Track the word-to-verse mapping for the current chapter
    private var wordToVerseMap: Map<Int, CurrentWordPosition> = emptyMap()

    init {
        viewModelScope.launch {
            aiEnabled = userPreferences.aiEnabled.first()
            val savedVoice = userPreferences.ttsVoiceName.first()
            if (savedVoice != null) {
                ttsManager.setVoice(savedVoice)
            }
            
            // Listen to word index changes from TTS
            ttsManager.currentWordIndex.collect { wordIndex ->
                wordIndex?.let { idx ->
                    currentWordPosition = wordToVerseMap[idx]
                } ?: run {
                    currentWordPosition = null
                }
            }
        }
    }

    fun toggleAudio() {
        if (isPlayingAudio) {
            ttsManager.stop()
            isPlayingAudio = false
            currentWordPosition = null
        } else {
            chapter?.let { ch ->
                // Construct text and build word-to-verse mapping
                val prefix = "${ch.book} Chapter ${ch.number}. "
                val verseTexts = ch.verses.map { it.text }
                val textToRead = prefix + verseTexts.joinToString(" ")
                
                // Build mapping: word index -> (verse number, word index in verse)
                buildWordToVerseMap(ch, prefix)
                
                ttsManager.speak(textToRead)
                isPlayingAudio = true
            }
        }
    }

    private fun buildWordToVerseMap(chapter: Chapter, prefix: String) {
        val map = mutableMapOf<Int, CurrentWordPosition>()
        var globalWordIndex = 0
        
        // Count words in prefix (e.g., "Genesis Chapter 1. ")
        val prefixWords = prefix.split(Regex("\\s+")).filter { it.isNotBlank() }
        globalWordIndex += prefixWords.size
        
        // Map words in each verse
        chapter.verses.forEach { verse ->
            val verseWords = verse.text.split(Regex("\\s+")).filter { it.isNotBlank() }
            verseWords.forEachIndexed { wordIndexInVerse, _ ->
                map[globalWordIndex] = CurrentWordPosition(
                    verseNumber = verse.number,
                    wordIndexInVerse = wordIndexInVerse
                )
                globalWordIndex++
            }
        }
        
        wordToVerseMap = map
    }

    fun stopTts() {
        ttsManager.stop()
        isPlayingAudio = false
        currentWordPosition = null
    }
    
    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }

    fun loadChapter(book: String, chapterNumber: Int) {
        viewModelScope.launch {
            try {
                chapterError = null
                chapter = bibleRepository.getChapter(book, chapterNumber)
                if (chapter == null) {
                    chapterError = "Failed to load chapter. Please check your internet connection."
                } else {
                    val progress = pathRepository.getProgressForChapter("$book-$chapterNumber")
                    isCompleted = progress?.isCompleted ?: false

                    // Track chapter read in Firebase
                    firebaseManager.logChapterRead(book, chapterNumber)
                }
            } catch (e: Exception) {
                chapterError = "Error loading chapter: ${e.message}"
                chapter = null
                // Log error to Crashlytics
                firebaseManager.logException(e)
            }
        }
    }
    
    fun explainVerse(text: String) {
        viewModelScope.launch {
            isGeneratingAi = true
            aiExplanation = null
            aiExplanation = ollamaRepository.getExplanation(text)
            isGeneratingAi = false
        }
    }
    
    fun clearAiExplanation() {
        aiExplanation = null
    }

    fun markAsCompleted() {
        chapter?.let { ch ->
            viewModelScope.launch {
                pathRepository.updateProgress(
                    ProgressEntity(
                        chapterId = "${ch.book}-${ch.number}",
                        bookName = ch.book,
                        chapter = ch.number,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                )
                userPreferences.markStudyComplete()
                // Save current position for faster getNextChapter()
                userPreferences.setCurrentStudyPosition(ch.book, ch.number)
                isCompleted = true

                // Track chapter completion in Firebase
                firebaseManager.logChapterCompleted(ch.book, ch.number)
                
                // Generate success message
                if (aiEnabled) {
                    isGeneratingCompletionMessage = true
                    completionMessage = null
                    try {
                        val aiMessage = ollamaRepository.generateCompletionMessage(ch.book, ch.number)
                        completionMessage = aiMessage ?: "Chapter completed! Great job!"
                    } catch (e: Exception) {
                        completionMessage = "Chapter completed! Great job!"
                    }
                    isGeneratingCompletionMessage = false
                } else {
                    completionMessage = "Chapter completed! Great job!"
                }
            }
        }
    }
    
    fun clearCompletionMessage() {
        completionMessage = null
    }

    fun saveNote(verseNumber: Int, content: String) {
        chapter?.let { ch ->
            viewModelScope.launch {
                pathRepository.insertNote(
                    NoteEntity(
                        bookName = ch.book,
                        chapter = ch.number,
                        verse = verseNumber,
                        content = content
                    )
                )

                // Track note creation in Firebase
                firebaseManager.logNoteCreated(ch.book, ch.number)
            }
        }
    }

    suspend fun isVerseFavorite(verse: Verse): Boolean {
        chapter?.let { ch ->
            val verseId = "${ch.book}-${ch.number}-${verse.number}"
            return pathRepository.isFavorite(verseId)
        }
        return false
    }

    fun toggleFavorite(verse: Verse) {
        chapter?.let { ch ->
            viewModelScope.launch {
                val verseId = "${ch.book}-${ch.number}-${verse.number}"
                val isFav = pathRepository.isFavorite(verseId)

                if (isFav) {
                    pathRepository.removeFavorite(verseId)
                    // Track favorite removed in Firebase
                    firebaseManager.logFavoriteRemoved(ch.book, ch.number, verse.number)
                } else {
                    pathRepository.addFavorite(
                        FavoriteEntity(
                            verseId = verseId,
                            bookName = ch.book,
                            chapter = ch.number,
                            verseNumber = verse.number,
                            verseText = verse.text
                        )
                    )
                    // Track favorite added in Firebase
                    firebaseManager.logFavoriteAdded(ch.book, ch.number, verse.number)
                }
            }
        }
    }
}
