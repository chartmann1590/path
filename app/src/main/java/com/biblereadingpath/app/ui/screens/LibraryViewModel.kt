package com.biblereadingpath.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.data.BibleBooks
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.repository.BibleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DownloadedBook(
    val name: String,
    val maxChapter: Int,
    val testament: BibleBooks.Testament,
    val expectedChapters: Int = 0  // Total chapters expected for the book
)

class LibraryViewModel(
    private val bibleRepository: BibleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var downloadedBooks by mutableStateOf<List<DownloadedBook>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    val currentTranslation: StateFlow<String> = userPreferences.translation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "web")

    init {
        loadDownloadedBooks()
        
        // Refresh downloaded books list when translation changes
        viewModelScope.launch {
            userPreferences.translation
                .distinctUntilChanged()
                .collectLatest {
                    loadDownloadedBooks()
                }
        }
    }

    fun loadDownloadedBooks() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Get all books and check which ones have local content
                val allBooks = BibleBooks.ALL_BOOKS
                val downloaded = mutableListOf<DownloadedBook>()

                allBooks.forEach { bookInfo ->
                    // Get the actual count of downloaded chapters
                    val downloadedChapterCount = bibleRepository.getDownloadedChapterCount(bookInfo.name)
                    if (downloadedChapterCount > 0) {
                        downloaded.add(
                            DownloadedBook(
                                name = bookInfo.name,
                                maxChapter = downloadedChapterCount,
                                testament = bookInfo.testament,
                                expectedChapters = bookInfo.chapters
                            )
                        )
                    }
                }

                downloadedBooks = downloaded
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}
