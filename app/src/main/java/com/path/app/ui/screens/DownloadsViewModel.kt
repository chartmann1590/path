package com.path.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.path.app.data.repository.BibleRepository
import kotlinx.coroutines.launch

data class BibleBook(
    val name: String,
    val chapters: Int,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f
)

class DownloadsViewModel(
    private val bibleRepository: BibleRepository
) : ViewModel() {

    var error by mutableStateOf<String?>(null)
        private set
    
    var bibleBooks by mutableStateOf(
        listOf(
            BibleBook("Genesis", 50),
            BibleBook("Exodus", 40),
            BibleBook("Leviticus", 27),
            BibleBook("Numbers", 36),
            BibleBook("Deuteronomy", 34),
            BibleBook("Joshua", 24),
            BibleBook("Judges", 21),
            BibleBook("Ruth", 4),
            BibleBook("1 Samuel", 31),
            BibleBook("2 Samuel", 24),
            BibleBook("Psalms", 150),
            BibleBook("Proverbs", 31),
            BibleBook("Ecclesiastes", 12),
            BibleBook("Isaiah", 66),
            BibleBook("Jeremiah", 52),
            BibleBook("Ezekiel", 48),
            BibleBook("Daniel", 12),
            BibleBook("Matthew", 28),
            BibleBook("Mark", 16),
            BibleBook("Luke", 24),
            BibleBook("John", 21),
            BibleBook("Acts", 28),
            BibleBook("Romans", 16),
            BibleBook("1 Corinthians", 16),
            BibleBook("2 Corinthians", 13),
            BibleBook("Galatians", 6),
            BibleBook("Ephesians", 6),
            BibleBook("Philippians", 4),
            BibleBook("Colossians", 4),
            BibleBook("1 Thessalonians", 5),
            BibleBook("2 Thessalonians", 3),
            BibleBook("1 Timothy", 6),
            BibleBook("2 Timothy", 4),
            BibleBook("Titus", 3),
            BibleBook("Philemon", 1),
            BibleBook("Hebrews", 13),
            BibleBook("James", 5),
            BibleBook("1 Peter", 5),
            BibleBook("2 Peter", 3),
            BibleBook("1 John", 5),
            BibleBook("2 John", 1),
            BibleBook("3 John", 1),
            BibleBook("Jude", 1),
            BibleBook("Revelation", 22)
        )
    )
        private set

    init {
        checkDownloadedBooks()
    }

    private fun checkDownloadedBooks() {
        viewModelScope.launch {
            // Check which books are already downloaded
            bibleBooks = bibleBooks.map { book ->
                var hasContent = false
                try {
                    val chapter = bibleRepository.getChapter(book.name, 1)
                    hasContent = chapter != null && chapter.verses.isNotEmpty()
                } catch (e: Exception) {
                    // Book not downloaded - not an error, just not cached
                }
                book.copy(isDownloaded = hasContent)
            }
        }
    }

    fun downloadBook(bookName: String) {
        viewModelScope.launch {
            // Update book status to downloading
            bibleBooks = bibleBooks.map {
                if (it.name == bookName) it.copy(isDownloading = true, downloadProgress = 0f)
                else it
            }

            val book = bibleBooks.find { it.name == bookName } ?: return@launch

            try {
                // Download each chapter
                for (chapter in 1..book.chapters) {
                    try {
                        bibleRepository.getChapter(bookName, chapter)

                        // Update progress
                        val progress = chapter.toFloat() / book.chapters
                        bibleBooks = bibleBooks.map {
                            if (it.name == bookName) it.copy(downloadProgress = progress)
                            else it
                        }
                    } catch (e: Exception) {
                        // Continue even if one chapter fails
                    }
                }

                // Mark as downloaded
                bibleBooks = bibleBooks.map {
                    if (it.name == bookName) {
                        it.copy(
                            isDownloaded = true,
                            isDownloading = false,
                            downloadProgress = 1f
                        )
                    } else it
                }
            } catch (e: Exception) {
                // Mark as failed
                bibleBooks = bibleBooks.map {
                    if (it.name == bookName) {
                        it.copy(isDownloading = false, downloadProgress = 0f)
                    } else it
                }
            }
        }
    }

    fun downloadAll() {
        bibleBooks.forEach { book ->
            if (!book.isDownloaded && !book.isDownloading) {
                downloadBook(book.name)
            }
        }
    }
}
