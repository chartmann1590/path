package com.biblereadingpath.app.data.repository

import android.content.Context
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.remote.OllamaApiService
import com.biblereadingpath.app.data.remote.OllamaRequest
import com.biblereadingpath.app.data.remote.OnDeviceLlmService
import com.google.gson.JsonParser
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

enum class AiProvider {
    OFF,
    GEMMA_ON_DEVICE,
    OLLAMA;

    companion object {
        fun fromId(id: String?): AiProvider = entries.firstOrNull { it.name == id } ?: OFF
    }
}

class AiRepository(
    private val userPreferences: UserPreferences,
    private val context: Context
) {
    private val onDeviceService = OnDeviceLlmService(context)

    suspend fun getActiveBackend(): AiBackend {
        return when (AiProvider.fromId(userPreferences.aiProvider.first())) {
            AiProvider.OFF -> AiBackend.NONE
            AiProvider.GEMMA_ON_DEVICE -> {
                val model = OnDeviceLlmService.GemmaModel.fromId(userPreferences.gemmaModel.first())
                val file = onDeviceService.getDownloadedModelFile(model)
                if (file != null && onDeviceService.initialize(file)) AiBackend.ON_DEVICE else AiBackend.NONE
            }
            AiProvider.OLLAMA -> {
                val url = userPreferences.ollamaUrl.first()
                if (!url.isNullOrBlank()) AiBackend.OLLAMA else AiBackend.NONE
            }
        }
    }

    suspend fun isAvailable(): Boolean = getActiveBackend() != AiBackend.NONE

    suspend fun explainVerse(verseText: String): String? {
        val prompt = "Explain this Bible verse briefly from a Christian devotional perspective: \"$verseText\""
        return generateText(prompt)
    }

    suspend fun generateReflection(book: String, chapter: Int): String? {
        val prompt = "Generate a brief devotional reflection question (1-2 sentences) for $book chapter $chapter. " +
            "The question should help the reader apply the passage to daily life. Just return the question."
        return generateText(prompt)
    }

    suspend fun generateCompletionMessage(book: String, chapterNumber: Int): String? {
        val prompt = "Generate a brief, encouraging success message (1-2 sentences) for completing $book chapter $chapterNumber. Keep it devotional and gentle. Just return the message."
        return generateText(prompt)?.trim()
    }

    suspend fun generateFavoriteSummary(reference: String, verseText: String): String? {
        val prompt = "Provide a brief devotional explanation of this Bible verse: $reference - \"$verseText\""
        return generateText(prompt)
    }

    suspend fun generateBookSummary(bookName: String): String? {
        val prompt = "Provide a brief overview of the book of $bookName from the Bible, including its main themes, purpose, and key messages."
        return generateText(prompt)
    }

    suspend fun generateChapterSummary(book: String, chapter: Int, chapterText: String): String? {
        val prompt = "Provide a brief devotional summary of $book chapter $chapter from the Bible. Here is the text: $chapterText"
        return generateText(prompt)
    }

    suspend fun generateQuiz(book: String, chapterNumber: Int, chapterText: String): Quiz? {
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

        val response = generateText(prompt) ?: return null
        return parseQuiz(response)
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

        val cleanUrl = normalizeOllamaUrl(urlInput)
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)

        return try {
            api.generate(OllamaRequest(model, prompt)).response
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeOllamaUrl(urlInput: String): String {
        var cleanUrl = urlInput.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }
        if (!cleanUrl.endsWith("/")) {
            cleanUrl = "$cleanUrl/"
        }
        return cleanUrl
    }

    private fun parseQuiz(rawText: String): Quiz? {
        val jsonText = rawText.trim()
        val json = when {
            jsonText.contains("```json") -> jsonText.substringAfter("```json").substringBefore("```").trim()
            jsonText.contains("```") -> jsonText.substringAfter("```").substringBefore("```").trim()
            else -> jsonText
        }

        return try {
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val questionsArray = jsonObject.getAsJsonArray("questions")

            val questions = mutableListOf<QuizQuestion>()
            for (i in 0 until questionsArray.size()) {
                val qObj = questionsArray[i].asJsonObject
                val questionText = qObj.get("question").asString
                val optionsArray = qObj.getAsJsonArray("options")
                val options = (0 until optionsArray.size()).map { optionsArray[it].asString }
                val correctIndex = qObj.get("correct").asInt

                if (options.size == 4 && correctIndex in 0..3) {
                    questions.add(QuizQuestion(questionText, options, correctIndex))
                }
            }

            if (questions.size == 5) Quiz(questions) else null
        } catch (e: Exception) {
            null
        }
    }

    fun getOnDeviceService(): OnDeviceLlmService = onDeviceService
}
