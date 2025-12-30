package com.biblereadingpath.app.data.repository

import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.remote.OllamaApiService
import com.biblereadingpath.app.data.remote.OllamaRequest
import com.google.gson.Gson
import com.google.gson.JsonParser
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

    suspend fun generateQuiz(book: String, chapterNumber: Int, chapterText: String): Quiz? {
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
            .readTimeout(90, TimeUnit.SECONDS)  // Longer timeout for quiz generation
            .retryOnConnectionFailure(true)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)

        // Build prompt requesting structured JSON
        val prompt = """Generate 5 multiple-choice questions about $book chapter $chapterNumber based on this text:

$chapterText

Return ONLY a valid JSON object in this exact format (no markdown, no code blocks, just the JSON):
{
  "questions": [
    {
      "question": "Question text here?",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correct": 0
    }
  ]
}

Where "correct" is the index (0-3) of the correct answer. Make questions test understanding of key themes, events, or teachings from the chapter."""

        return try {
            val response = api.generate(OllamaRequest(model, prompt))
            val jsonText = response.response.trim()
            
            // Try to extract JSON if wrapped in markdown code blocks
            val json = if (jsonText.contains("```json")) {
                jsonText.substringAfter("```json").substringBefore("```").trim()
            } else if (jsonText.contains("```")) {
                jsonText.substringAfter("```").substringBefore("```").trim()
            } else {
                jsonText
            }
            
            // Parse JSON
            val gson = Gson()
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val questionsArray = jsonObject.getAsJsonArray("questions")
            
            val questions = mutableListOf<QuizQuestion>()
            for (i in 0 until questionsArray.size()) {
                val qObj = questionsArray[i].asJsonObject
                val questionText = qObj.get("question").asString
                val optionsArray = qObj.getAsJsonArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsArray.size()) {
                    options.add(optionsArray[j].asString)
                }
                val correctIndex = qObj.get("correct").asInt
                
                if (options.size == 4 && correctIndex in 0..3) {
                    questions.add(QuizQuestion(questionText, options, correctIndex))
                }
            }
            
            if (questions.size == 5) {
                Quiz(questions)
            } else {
                null // Invalid quiz format
            }
        } catch (e: Exception) {
            null // Return null on error
        }
    }
}
