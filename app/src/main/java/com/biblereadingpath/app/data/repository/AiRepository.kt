package com.biblereadingpath.app.data.repository

import android.content.Context
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.remote.OnDeviceLlmService
import com.biblereadingpath.app.data.remote.OllamaApiService
import com.biblereadingpath.app.data.remote.OllamaRequest
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

enum class AiBackend {
    NONE,
    ON_DEVICE,
    OLLAMA
}

class AiRepository(
    private val userPreferences: UserPreferences,
    private val context: Context
) {
    private val onDeviceService = OnDeviceLlmService(context)

    suspend fun getActiveBackend(): AiBackend {
        if (onDeviceService.isReady()) return AiBackend.ON_DEVICE
        val aiEnabled = userPreferences.aiEnabled.first()
        if (aiEnabled) {
            val url = userPreferences.ollamaUrl.first()
            if (!url.isNullOrBlank()) return AiBackend.OLLAMA
        }
        return AiBackend.NONE
    }

    suspend fun isAvailable(): Boolean = getActiveBackend() != AiBackend.NONE

    suspend fun explainVerse(verseText: String): String? {
        val prompt = "Explain this Bible verse briefly from a Christian devotional perspective: \"$verseText\""
        return generateText(prompt)
    }

    suspend fun generateReflection(book: String, chapter: Int): String? {
        val prompt = "Generate a brief devotional reflection question (1-2 sentences) for $book chapter $chapter. " +
            "The question should help the reader apply the passage to their daily life. Just return the question."
        return generateText(prompt)
    }

    suspend fun generateCompletionMessage(book: String, chapterNumber: Int): String? {
        val prompt = "Generate a brief, encouraging success message (1-2 sentences) for completing $book chapter $chapterNumber. Keep it devotional and gentle."
        val result = generateText(prompt) ?: return null
        return result.trim()
    }

    suspend fun generateQuizPrompt(book: String, chapterNumber: Int, chapterText: String): String? {
        val prompt = "Generate 5 multiple-choice questions about $book chapter $chapterNumber based on this text:\n\n$chapterText\n\n" +
            "Return ONLY valid JSON: {\"questions\":[{\"question\":\"...\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correct\":0}]}"
        return generateText(prompt)
    }

    private suspend fun generateText(prompt: String): String? {
        val backend = getActiveBackend()
        return when (backend) {
            AiBackend.ON_DEVICE -> onDeviceService.generate(prompt)
            AiBackend.OLLAMA -> generateViaOllama(prompt)
            AiBackend.NONE -> null
        }
    }

    private suspend fun generateViaOllama(prompt: String): String? {
        val urlInput = userPreferences.ollamaUrl.first() ?: return null
        val model = userPreferences.ollamaModel.first() ?: "llama2"

        var cleanUrl = urlInput.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }
        if (!cleanUrl.endsWith("/")) {
            cleanUrl = "$cleanUrl/"
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)

        return try {
            val response = api.generate(OllamaRequest(model, prompt))
            response.response
        } catch (e: Exception) {
            null
        }
    }

    fun getOnDeviceService(): OnDeviceLlmService = onDeviceService
}
