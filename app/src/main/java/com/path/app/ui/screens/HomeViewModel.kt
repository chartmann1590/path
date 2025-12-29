package com.path.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.path.app.data.preferences.UserPreferences
import com.path.app.data.repository.BibleRepository
import com.path.app.data.repository.PathRepository
import com.path.app.domain.model.Verse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPreferences: UserPreferences,
    private val pathRepository: PathRepository,
    private val bibleRepository: BibleRepository
) : ViewModel() {
    val streak: StateFlow<Int> = userPreferences.streak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _nextChapter = MutableStateFlow("Genesis 1")
    val nextChapter = _nextChapter.asStateFlow()
    
    // Store as separate fields for button click
    var nextBookStr = "Genesis"
    var nextChapterInt = 1

    private val _verseOfTheDay = MutableStateFlow<Verse?>(null)
    val verseOfTheDay = _verseOfTheDay.asStateFlow()
    
    private val _verseOfTheDayError = MutableStateFlow<String?>(null)
    val verseOfTheDayError = _verseOfTheDayError.asStateFlow()
    
    private val _nextChapterError = MutableStateFlow<String?>(null)
    val nextChapterError = _nextChapterError.asStateFlow()

    init {
        loadVerseOfTheDay()
        loadNextChapter()
    }
    
    fun loadNextChapter() {
        viewModelScope.launch {
            try {
                _nextChapterError.value = null
                val (book, chap) = pathRepository.getNextChapter()
                nextBookStr = book
                nextChapterInt = chap
                _nextChapter.value = "$book $chap"
            } catch (e: Exception) {
                _nextChapterError.value = "Failed to load next chapter: ${e.message}"
            }
        }
    }

    fun loadVerseOfTheDay() {
        viewModelScope.launch {
            // Check if we already have today's verse
            val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24) // Days since epoch
            val storedDate = userPreferences.vodDate.first()
            val storedText = userPreferences.vodText.first()
            val storedRef = userPreferences.vodRef.first()
            
            if (storedDate == today && storedText != null && storedRef != null) {
                // Use today's cached verse - parse reference like "Genesis 1:1"
                try {
                    val parts = storedRef.split(" ")
                    if (parts.size >= 2) {
                        val book = parts.dropLast(1).joinToString(" ")
                        val chapterVerse = parts.last().split(":")
                        if (chapterVerse.size == 2) {
                            val chapter = chapterVerse[0].toIntOrNull() ?: 1
                            val verseNum = chapterVerse[1].toIntOrNull() ?: 1
                            _verseOfTheDay.value = Verse(book, chapter, verseNum, storedText)
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    // Fall through to load new verse if parsing fails
                }
            }
            
            // Load new verse for today
            try {
                _verseOfTheDayError.value = null
                val verse = bibleRepository.getRandomVerse()
                _verseOfTheDay.value = verse
                if (verse != null) {
                    userPreferences.saveVerseOfTheDay(verse.text, "${verse.book} ${verse.chapter}:${verse.number}", today)
                } else {
                    _verseOfTheDayError.value = "Failed to load verse of the day"
                }
            } catch (e: Exception) {
                _verseOfTheDayError.value = "Failed to load verse of the day: ${e.message}"
            }
        }
    }
}
