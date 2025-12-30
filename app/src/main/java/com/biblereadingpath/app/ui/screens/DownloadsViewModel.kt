package com.biblereadingpath.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.data.AvailableBooks
import com.biblereadingpath.app.data.BibleBooks
import com.biblereadingpath.app.data.BibleTranslations
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

data class BibleBook(
    val name: String,
    val chapters: Int,
    val testament: BibleBooks.Testament,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val isAvailable: Boolean = true,
    val downloadedChapters: Int = 0  // Track how many chapters are actually downloaded
)

class DownloadsViewModel(
    private val bibleRepository: BibleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var error by mutableStateOf<String?>(null)

    var downloadStats by mutableStateOf("")
        private set

    val currentTranslation: StateFlow<String> = userPreferences.translation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "web")

    var bibleBooks by mutableStateOf(
        // Only show books that are available from the API
        AvailableBooks.getAvailableBooks().map { bookInfo ->
            BibleBook(
                name = bookInfo.name,
                chapters = bookInfo.chapters,
                testament = bookInfo.testament,
                isAvailable = true
            )
        }
    )
        private set

    init {
        checkDownloadedBooks()
        
        // Refresh downloaded books list when translation changes
        viewModelScope.launch {
            userPreferences.translation
                .distinctUntilChanged()
                .collectLatest {
                    checkDownloadedBooks()
                }
        }
    }

    private fun checkDownloadedBooks() {
        viewModelScope.launch {
            // Check download status for all books (full or partial)
            bibleBooks = bibleBooks.map { book ->
                val downloadedChapterCount = try {
                    bibleRepository.getDownloadedChapterCount(book.name)
                } catch (e: Exception) {
                    0
                }
                val isFullyDownloaded = downloadedChapterCount == book.chapters

                book.copy(
                    isDownloaded = isFullyDownloaded,
                    downloadedChapters = downloadedChapterCount
                )
            }
        }
    }

    fun downloadBook(bookName: String) {
        viewModelScope.launch {
            android.util.Log.d("DownloadsVM", "Starting download for: $bookName")

            // Update book status to downloading
            bibleBooks = bibleBooks.map {
                if (it.name == bookName) it.copy(isDownloading = true, downloadProgress = 0f)
                else it
            }

            val book = bibleBooks.find { it.name == bookName } ?: return@launch

            var successfulChapters = 0
            var failedChapters = 0
            val failedChapterNumbers = mutableListOf<Int>()

            try {
                // Download each chapter
                for (chapter in 1..book.chapters) {
                    try {
                        android.util.Log.d("DownloadsVM", "Downloading $bookName chapter $chapter")
                        // Use fetchChapterForDownload to bypass cache/assets and ensure DB save
                        val result = bibleRepository.fetchChapterForDownload(bookName, chapter)

                        if (result != null && result.verses.isNotEmpty()) {
                            successfulChapters++
                            android.util.Log.d("DownloadsVM", "Success: $bookName $chapter (${result.verses.size} verses)")
                        } else {
                            failedChapters++
                            failedChapterNumbers.add(chapter)
                            android.util.Log.e("DownloadsVM", "Failed: $bookName $chapter - null or empty")
                        }

                        // Update progress
                        val progress = chapter.toFloat() / book.chapters
                        bibleBooks = bibleBooks.map {
                            if (it.name == bookName) it.copy(downloadProgress = progress)
                            else it
                        }

                        // Delay to avoid rate limiting (API has strict rate limits)
                        // Increased delay for larger books to reduce rate limit issues
                        val delayMs = if (book.chapters > 20) 2000L else 1500L
                        kotlinx.coroutines.delay(delayMs)
                    } catch (e: Exception) {
                        failedChapters++
                        failedChapterNumbers.add(chapter)
                        android.util.Log.e("DownloadsVM", "Exception downloading $bookName $chapter: ${e.message}", e)
                    }
                }

                // Retry failed chapters with longer delays
                if (failedChapterNumbers.isNotEmpty()) {
                    android.util.Log.w("DownloadsVM", "Retrying ${failedChapterNumbers.size} failed chapters for $bookName")
                    kotlinx.coroutines.delay(5000) // Wait 5 seconds before retrying to avoid rate limits
                    
                    val retryFailed = mutableListOf<Int>()
                    for (chapter in failedChapterNumbers) {
                        try {
                            android.util.Log.d("DownloadsVM", "Retrying $bookName chapter $chapter")
                            val result = bibleRepository.fetchChapterForDownload(bookName, chapter)
                            
                            if (result != null && result.verses.isNotEmpty()) {
                                successfulChapters++
                                android.util.Log.d("DownloadsVM", "Retry success: $bookName $chapter (${result.verses.size} verses)")
                            } else {
                                retryFailed.add(chapter)
                                android.util.Log.e("DownloadsVM", "Retry failed: $bookName $chapter")
                            }
                            
                            // Longer delay between retries
                            kotlinx.coroutines.delay(3000)
                        } catch (e: Exception) {
                            retryFailed.add(chapter)
                            android.util.Log.e("DownloadsVM", "Retry exception for $bookName $chapter: ${e.message}", e)
                            kotlinx.coroutines.delay(3000)
                        }
                    }
                    
                    if (retryFailed.isNotEmpty()) {
                        android.util.Log.w("DownloadsVM", "Still failed after retry: ${retryFailed.size} chapters for $bookName")
                    }
                }

                android.util.Log.d("DownloadsVM", "Download complete for $bookName: $successfulChapters success, $failedChapters failed")

                // Wait for DB operations to complete - longer delay for larger books
                val delayMs = if (book.chapters > 20) 2000L else 1000L
                android.util.Log.d("DownloadsVM", "Waiting ${delayMs}ms for DB operations to complete for $bookName")
                kotlinx.coroutines.delay(delayMs)

                // Verify download status - try multiple times if needed
                var downloadedChapterCount = bibleRepository.getDownloadedChapterCount(bookName)
                var verificationAttempts = 0
                val maxVerificationAttempts = 3
                
                while (downloadedChapterCount < successfulChapters && verificationAttempts < maxVerificationAttempts) {
                    verificationAttempts++
                    android.util.Log.w("DownloadsVM", "Verification attempt $verificationAttempts: Found $downloadedChapterCount chapters, expected $successfulChapters. Retrying...")
                    kotlinx.coroutines.delay(1000)
                    downloadedChapterCount = bibleRepository.getDownloadedChapterCount(bookName)
                }
                
                val isFullyDownloaded = downloadedChapterCount == book.chapters
                android.util.Log.d("DownloadsVM", "Verification for $bookName: isFullyDownloaded=$isFullyDownloaded, downloadedChapters=$downloadedChapterCount/${book.chapters}, successfulDownloads=$successfulChapters")

                // Update book status with actual downloaded chapter count
                bibleBooks = bibleBooks.map {
                    if (it.name == bookName) {
                        it.copy(
                            isDownloaded = isFullyDownloaded,
                            isDownloading = false,
                            downloadProgress = if (isFullyDownloaded) 1f else 0f,
                            downloadedChapters = downloadedChapterCount
                        )
                    } else it
                }

                // Log download result
                if (!isFullyDownloaded) {
                    val msg = "Failed to download $bookName ($successfulChapters/${book.chapters} chapters)"
                    android.util.Log.e("DownloadsVM", msg)
                    error = msg
                } else {
                    android.util.Log.d("DownloadsVM", "Successfully downloaded $bookName")
                }
            } catch (e: Exception) {
                android.util.Log.e("DownloadsVM", "Fatal error downloading $bookName: ${e.message}", e)
                e.printStackTrace()
                error = "Error downloading $bookName: ${e.message}"

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
        viewModelScope.launch {
            error = null
            val booksToDownload = bibleBooks.filter { !it.isDownloaded && !it.isDownloading }
            val totalBooks = booksToDownload.size
            var completedBooks = 0
            var successfulBooks = 0

            booksToDownload.forEach { book ->
                downloadBook(book.name)
                completedBooks++

                // Check if download was successful (fully downloaded)
                kotlinx.coroutines.delay(300)
                if (bibleRepository.isBookFullyDownloaded(book.name, book.chapters)) {
                    successfulBooks++
                }

                downloadStats = "Downloaded: $successfulBooks/$completedBooks of $totalBooks books"

                // Longer delay between books to avoid overwhelming the API
                kotlinx.coroutines.delay(2000)
            }

            // Show final summary
            error = "Download complete: $successfulBooks of $totalBooks books downloaded successfully"
            downloadStats = ""
        }
    }
}
