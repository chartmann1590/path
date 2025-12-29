package com.path.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.path.app.analytics.FirebaseManager
import com.path.app.data.local.entity.FavoriteEntity
import com.path.app.data.repository.BibleRepository
import com.path.app.data.repository.PathRepository
import com.path.app.domain.model.Verse
import kotlinx.coroutines.launch

data class SearchResult(
    val book: String,
    val chapter: Int,
    val verse: Verse,
    val isFavorite: Boolean = false
)

class SearchViewModel(
    private val bibleRepository: BibleRepository,
    private val pathRepository: PathRepository,
    private val firebaseManager: FirebaseManager
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<SearchResult>>(emptyList())
        private set

    var isSearching by mutableStateOf(false)
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
        if (query.length >= 3) {
            performSearch(query)
        } else {
            searchResults = emptyList()
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            isSearching = true
            try {
                val results = mutableListOf<SearchResult>()

                // Split query into keywords for smarter matching
                val keywords = query.lowercase().split(" ").filter { it.length >= 2 }

                // Search more books for better results
                val searchableBooks = mapOf(
                    "Genesis" to 50, "Exodus" to 40, "Psalms" to 150, "Proverbs" to 31,
                    "Isaiah" to 66, "Matthew" to 28, "Mark" to 16, "Luke" to 24,
                    "John" to 21, "Acts" to 28, "Romans" to 16, "1 Corinthians" to 16,
                    "2 Corinthians" to 13, "Galatians" to 6, "Ephesians" to 6,
                    "Philippians" to 4, "Colossians" to 4, "1 Thessalonians" to 5,
                    "James" to 5, "1 Peter" to 5, "1 John" to 5, "Revelation" to 22
                )

                searchableBooks.forEach { (book, maxChapter) ->
                    for (chapter in 1..maxChapter) {
                        try {
                            val chapterData = bibleRepository.getChapter(book, chapter)
                            chapterData?.verses?.forEach { verse ->
                                val verseTextLower = verse.text.lowercase()

                                // Smart matching: if query contains multiple words, match if verse contains most of them
                                val matchScore = if (keywords.size > 1) {
                                    keywords.count { keyword -> verseTextLower.contains(keyword) }
                                } else {
                                    if (verseTextLower.contains(query.lowercase())) 1 else 0
                                }

                                // Accept if at least 70% of keywords match (or 2+ keywords for short queries)
                                val threshold = if (keywords.size <= 3) 2 else (keywords.size * 0.7).toInt()
                                if (matchScore >= threshold || matchScore >= keywords.size) {
                                    results.add(
                                        SearchResult(
                                            book = book,
                                            chapter = chapter,
                                            verse = verse
                                        )
                                    )
                                }
                            }
                            // Return early if we have enough results
                            if (results.size >= 50) {
                                searchResults = results.sortedByDescending { result ->
                                    val verseTextLower = result.verse.text.lowercase()
                                    keywords.count { verseTextLower.contains(it) }
                                }
                                return@launch
                            }
                        } catch (e: Exception) {
                            // Skip chapters that don't exist
                        }
                    }
                }

                // Sort results by relevance (number of matching keywords)
                searchResults = results.sortedByDescending { result ->
                    val verseTextLower = result.verse.text.lowercase()
                    keywords.count { verseTextLower.contains(it) }
                }

                // Track search in Firebase Analytics
                firebaseManager.logSearch(query, searchResults.size)
            } catch (e: Exception) {
                searchResults = emptyList()
                // Log error to Crashlytics
                firebaseManager.logException(e)
            } finally {
                isSearching = false
            }
        }
    }

    fun clearSearch() {
        searchQuery = ""
        searchResults = emptyList()
    }

    fun toggleFavorite(result: SearchResult) {
        viewModelScope.launch {
            val verseId = "${result.book}-${result.chapter}-${result.verse.number}"
            val isFav = pathRepository.isFavorite(verseId)

            if (isFav) {
                pathRepository.removeFavorite(verseId)
            } else {
                pathRepository.addFavorite(
                    FavoriteEntity(
                        verseId = verseId,
                        bookName = result.book,
                        chapter = result.chapter,
                        verseNumber = result.verse.number,
                        verseText = result.verse.text
                    )
                )
            }

            // Update search results to reflect change
            searchResults = searchResults.map {
                if (it.book == result.book && it.chapter == result.chapter && it.verse.number == result.verse.number) {
                    it.copy(isFavorite = !isFav)
                } else {
                    it
                }
            }
        }
    }
}
