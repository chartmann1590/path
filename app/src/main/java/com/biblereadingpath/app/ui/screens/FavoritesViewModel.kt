package com.biblereadingpath.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.analytics.FirebaseManager
import com.biblereadingpath.app.data.local.entity.FavoriteEntity
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.repository.OllamaRepository
import com.biblereadingpath.app.data.repository.PathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val pathRepository: PathRepository,
    private val userPreferences: UserPreferences,
    private val firebaseManager: FirebaseManager,
    private val context: Context
) : ViewModel() {

    private val ollamaRepository = OllamaRepository(userPreferences)

    val favorites = pathRepository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var aiEnabled by mutableStateOf(false)
        private set

    var aiSummary by mutableStateOf<String?>(null)
        private set

    var isGeneratingAi by mutableStateOf(false)
        private set

    var selectedVerse by mutableStateOf<FavoriteEntity?>(null)
        private set

    val currentTranslation: StateFlow<String> = userPreferences.translation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "web")

    init {
        viewModelScope.launch {
            aiEnabled = userPreferences.aiEnabled.first()
        }
    }

    fun removeFavorite(verseId: String) {
        viewModelScope.launch {
            pathRepository.removeFavorite(verseId)
            // Parse verseId to get book, chapter, verse for analytics
            val parts = verseId.split("-")
            if (parts.size >= 3) {
                firebaseManager.logFavoriteRemoved(parts[0], parts[1].toIntOrNull() ?: 0, parts[2].toIntOrNull() ?: 0)
            }
        }
    }

    fun shareVerse(favorite: FavoriteEntity) {
        val shareText = buildString {
            appendLine("\"${favorite.verseText}\"")
            appendLine()
            appendLine("${favorite.bookName} ${favorite.chapter}:${favorite.verseNumber}")
            appendLine()
            appendLine("Shared from Path Bible Study App")
        }

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(intent, "Share verse")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)

        // Track verse share in Firebase
        firebaseManager.logVerseShared(favorite.bookName, favorite.chapter, favorite.verseNumber)
    }

    fun generateAiSummary(favorite: FavoriteEntity) {
        if (!aiEnabled) return

        viewModelScope.launch {
            isGeneratingAi = true
            selectedVerse = favorite
            aiSummary = null

            try {
                val prompt = "Provide a brief devotional explanation of this Bible verse: ${favorite.bookName} ${favorite.chapter}:${favorite.verseNumber} - \"${favorite.verseText}\""

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

                // Track AI summary generation in Firebase
                firebaseManager.logAiSummaryGenerated("verse", favorite.bookName, favorite.chapter)
            } catch (e: Exception) {
                aiSummary = "Error generating summary: ${e.message}"
                // Log error to Crashlytics
                firebaseManager.logException(e)
            } finally {
                isGeneratingAi = false
            }
        }
    }

    fun dismissAiSummary() {
        aiSummary = null
        selectedVerse = null
    }
}
