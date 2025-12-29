package com.path.app.data.repository

import com.path.app.data.preferences.UserPreferences
import com.path.app.data.remote.OllamaApiService
import com.path.app.data.remote.OllamaRequest
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class OllamaRepository(
    private val userPreferences: UserPreferences
) {
    suspend fun fetchAvailableModels(baseUrlInput: String): List<String> {
        // Robust URL handling
        var cleanUrl = baseUrlInput.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }
        if (!cleanUrl.endsWith("/")) {
            cleanUrl = "$cleanUrl/"
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Increased for remote servers
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)

        val response = api.getTags()
        return response.models.map { it.name }
    }

    suspend fun getExplanation(text: String): String {
        val urlInput = userPreferences.ollamaUrl.first() ?: "http://localhost:11434"
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
            .readTimeout(60, TimeUnit.SECONDS)  // Longer timeout for AI generation
            .retryOnConnectionFailure(true)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)

        val prompt = "Explain this Bible verse briefly from a Christian devotional perspective: \"$text\""

        return try {
            val response = api.generate(OllamaRequest(model, prompt))
            response.response
        } catch (e: Exception) {
            "Error connecting to AI: ${e.message}. Please check your URL and ensure Ollama is running."
        }
    }
    
    suspend fun generateCompletionMessage(book: String, chapterNumber: Int): String? {
        val urlInput = userPreferences.ollamaUrl.first() ?: "http://localhost:11434"
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
            .readTimeout(30, TimeUnit.SECONDS)  // Shorter timeout for quick messages
            .retryOnConnectionFailure(true)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)

        val prompt = "Generate a brief, encouraging success message (1-2 sentences) for completing $book chapter $chapterNumber. Keep it devotional and gentle. Just return the message, no explanation."

        return try {
            val response = api.generate(OllamaRequest(model, prompt))
            response.response.trim()
        } catch (e: Exception) {
            null // Return null on error to allow fallback
        }
    }
}
