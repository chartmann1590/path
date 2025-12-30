package com.biblereadingpath.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.repository.BibleRepository
import com.biblereadingpath.app.data.repository.OllamaRepository
import com.biblereadingpath.app.data.repository.PathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookProgress(
    val name: String,
    val totalChapters: Int,
    val completedChapters: Int,
    val chapters: List<ChapterProgress>
)

data class ChapterProgress(
    val number: Int,
    val isCompleted: Boolean
)

class RoadmapViewModel(
    private val pathRepository: PathRepository,
    private val bibleRepository: BibleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var books by mutableStateOf<List<BookProgress>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    var aiEnabled by mutableStateOf(false)
        private set

    var isGeneratingAiSummary by mutableStateOf(false)
        private set

    var aiSummary by mutableStateOf<String?>(null)
        private set

    var selectedBookChapter by mutableStateOf<Pair<String, Int>?>(null)
        private set

    val currentTranslation: StateFlow<String> = userPreferences.translation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "web")

    private val bookChapterCounts = mapOf(
        "Genesis" to 50, "Exodus" to 40, "Leviticus" to 27, "Numbers" to 36, "Deuteronomy" to 34,
        "Joshua" to 24, "Judges" to 21, "Ruth" to 4, "1 Samuel" to 31, "2 Samuel" to 24,
        "1 Kings" to 22, "2 Kings" to 25, "1 Chronicles" to 29, "2 Chronicles" to 36, "Ezra" to 10,
        "Nehemiah" to 13, "Esther" to 10, "Job" to 42, "Psalms" to 150, "Proverbs" to 31,
        "Ecclesiastes" to 12, "Song of Solomon" to 8, "Isaiah" to 66, "Jeremiah" to 52,
        "Lamentations" to 5, "Ezekiel" to 48, "Daniel" to 12, "Hosea" to 14, "Joel" to 3,
        "Amos" to 9, "Obadiah" to 1, "Jonah" to 4, "Micah" to 7, "Nahum" to 3, "Habakkuk" to 3,
        "Zephaniah" to 3, "Haggai" to 2, "Zechariah" to 14, "Malachi" to 4,
        "Matthew" to 28, "Mark" to 16, "Luke" to 24, "John" to 21, "Acts" to 28,
        "Romans" to 16, "1 Corinthians" to 16, "2 Corinthians" to 13, "Galatians" to 6,
        "Ephesians" to 6, "Philippians" to 4, "Colossians" to 4, "1 Thessalonians" to 5,
        "2 Thessalonians" to 3, "1 Timothy" to 6, "2 Timothy" to 4, "Titus" to 3,
        "Philemon" to 1, "Hebrews" to 13, "James" to 5, "1 Peter" to 5, "2 Peter" to 3,
        "1 John" to 5, "2 John" to 1, "3 John" to 1, "Jude" to 1, "Revelation" to 22
    )

    init {
        loadRoadmap()
    }

    private fun loadRoadmap() {
        viewModelScope.launch {
            isLoading = true
            try {
                aiEnabled = userPreferences.aiEnabled.first()
                val allProgress = pathRepository.getAllProgress().first()
                val progressMap = allProgress.associateBy { it.chapterId }

                books = pathRepository.bibleBooks.mapNotNull { bookName ->
                    val chapterCount = bookChapterCounts[bookName] ?: return@mapNotNull null

                    val chapters = (1..chapterCount).map { chapterNum ->
                        val chapterId = "$bookName-$chapterNum"
                        val isCompleted = progressMap[chapterId]?.isCompleted ?: false
                        ChapterProgress(chapterNum, isCompleted)
                    }

                    BookProgress(
                        name = bookName,
                        totalChapters = chapterCount,
                        completedChapters = chapters.count { it.isCompleted },
                        chapters = chapters
                    )
                }
            } catch (e: Exception) {
                error = "Failed to load roadmap: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun generateBookSummary(bookName: String) {
        if (!aiEnabled) return

        viewModelScope.launch {
            isGeneratingAiSummary = true
            selectedBookChapter = Pair(bookName, 0) // 0 indicates book summary
            aiSummary = null

            try {
                val ollamaRepository = OllamaRepository(userPreferences)
                val prompt = "Provide a brief overview of the book of $bookName from the Bible, including its main themes, purpose, and key messages."

                val urlInput = userPreferences.ollamaUrl.first() ?: "http://localhost:11434"
                val model = userPreferences.ollamaModel.first() ?: "llama2"

                var cleanUrl = urlInput.trim()
                if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                    cleanUrl = "http://$cleanUrl"
                }
                if (!cleanUrl.endsWith("/")) {
                    cleanUrl = "$cleanUrl/"
                }

                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()

                val api = retrofit2.Retrofit.Builder()
                    .baseUrl(cleanUrl)
                    .client(client)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build()
                    .create(com.biblereadingpath.app.data.remote.OllamaApiService::class.java)

                val response = api.generate(com.biblereadingpath.app.data.remote.OllamaRequest(model, prompt))
                aiSummary = response.response
            } catch (e: Exception) {
                aiSummary = "Error generating summary: ${e.message}"
            } finally {
                isGeneratingAiSummary = false
            }
        }
    }

    fun generateChapterSummary(bookName: String, chapter: Int) {
        if (!aiEnabled) return

        viewModelScope.launch {
            isGeneratingAiSummary = true
            selectedBookChapter = Pair(bookName, chapter)
            aiSummary = null

            try {
                // Fetch the chapter content first
                val chapterData = bibleRepository.getChapter(bookName, chapter)
                if (chapterData == null) {
                    aiSummary = "Chapter not available. Please download this book first."
                    isGeneratingAiSummary = false
                    return@launch
                }

                val chapterText = chapterData.verses.joinToString(" ") { it.text }
                val prompt = "Provide a brief devotional summary of $bookName chapter $chapter from the Bible. Here is the text: $chapterText"

                val urlInput = userPreferences.ollamaUrl.first() ?: "http://localhost:11434"
                val model = userPreferences.ollamaModel.first() ?: "llama2"

                var cleanUrl = urlInput.trim()
                if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                    cleanUrl = "http://$cleanUrl"
                }
                if (!cleanUrl.endsWith("/")) {
                    cleanUrl = "$cleanUrl/"
                }

                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()

                val api = retrofit2.Retrofit.Builder()
                    .baseUrl(cleanUrl)
                    .client(client)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build()
                    .create(com.biblereadingpath.app.data.remote.OllamaApiService::class.java)

                val response = api.generate(com.biblereadingpath.app.data.remote.OllamaRequest(model, prompt))
                aiSummary = response.response
            } catch (e: Exception) {
                aiSummary = "Error generating summary: ${e.message}"
            } finally {
                isGeneratingAiSummary = false
            }
        }
    }

    fun dismissAiSummary() {
        aiSummary = null
        selectedBookChapter = null
    }

    fun refresh() {
        loadRoadmap()
    }
}
