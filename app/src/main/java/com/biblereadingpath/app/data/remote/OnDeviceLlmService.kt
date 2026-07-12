package com.biblereadingpath.app.data.remote

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class OnDeviceLlmService(
    private val context: Context
) {
    enum class ModelStatus {
        NOT_DOWNLOADED,
        DOWNLOADING,
        READY,
        ERROR
    }

    enum class GemmaModel(
        val id: String,
        val displayName: String,
        val fileName: String,
        val downloadUrl: String,
        val approximateSize: String
    ) {
        E2B(
            id = "E2B",
            displayName = "Gemma 4 E2B",
            fileName = "gemma-4-E2B-it.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true",
            approximateSize = "about 2.6 GB"
        ),
        E4B(
            id = "E4B",
            displayName = "Gemma 4 E4B",
            fileName = "gemma-4-E4B-it.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm?download=true",
            approximateSize = "about 3.7 GB"
        );

        companion object {
            fun fromId(id: String?): GemmaModel = entries.firstOrNull { it.id == id } ?: E2B
        }
    }

    data class ModelState(
        val status: ModelStatus,
        val modelName: String = "",
        val progress: Float = 0f,
        val error: String? = null
    )

    private val inferenceMutex = Mutex()
    private var engine: Engine? = null
    private var initializedModelPath: String? = null

    fun isReady(): Boolean = engine != null && initializedModelPath != null

    fun getModelDirectory(): File {
        return File(context.filesDir, "ai_models").also { it.mkdirs() }
    }

    fun getModelFile(model: GemmaModel): File = File(getModelDirectory(), model.fileName)

    fun getDownloadedModels(): List<String> {
        return getModelDirectory().listFiles()
            ?.filter { it.extension == "litertlm" || it.extension == "task" || it.extension == "tflite" || it.name.contains("gemma", ignoreCase = true) }
            ?.map { it.name }
            ?: emptyList()
    }

    fun getDownloadedModelFile(model: GemmaModel): File? {
        val file = getModelFile(model)
        return if (file.exists() && file.length() > 0L) file else null
    }

    suspend fun initialize(modelFile: File): Boolean = withContext(Dispatchers.IO) {
        if (!modelFile.exists()) {
            release()
            return@withContext false
        }

        inferenceMutex.withLock {
            if (initializedModelPath == modelFile.absolutePath && engine != null) {
                return@withLock true
            }

            releaseLocked()

            return@withLock try {
                val engineConfig = EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.GPU(),
                    cacheDir = context.cacheDir.absolutePath
                )
                engine = Engine(engineConfig).also { it.initialize() }
                initializedModelPath = modelFile.absolutePath
                true
            } catch (e: Exception) {
                releaseLocked()
                false
            }
        }
    }

    suspend fun generate(prompt: String): String? = withContext(Dispatchers.IO) {
        inferenceMutex.withLock {
            val activeEngine = engine ?: return@withLock null
            try {
                val config = ConversationConfig(
                    systemInstruction = Contents.of(
                        "You are a concise Christian Bible study assistant. Keep answers devotional, clear, and grounded in the provided passage."
                    ),
                    samplerConfig = SamplerConfig(
                        topK = 40,
                        topP = 0.95,
                        temperature = 0.7
                    )
                )
                activeEngine.createConversation(config).use { conversation ->
                    conversation.sendMessage(prompt)
                        .contents
                        .contents
                        .filterIsInstance<Content.Text>()
                        .joinToString(separator = "") { it.text }
                        .trim()
                        .ifBlank { null }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun downloadModel(
        model: GemmaModel,
        onProgress: (Float) -> Unit
    ): File? = downloadModel(model.downloadUrl, model.fileName, onProgress)

    suspend fun downloadModel(
        modelUrl: String,
        modelName: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val dir = getModelDirectory()
        val targetFile = File(dir, modelName)
        val tempFile = File(dir, "$modelName.download")

        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build()

            val request = Request.Builder().url(modelUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val responseBody = response.body ?: return@withContext null
                val contentLength = responseBody.contentLength()

                responseBody.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var totalRead = 0L
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (contentLength > 0) {
                                onProgress(totalRead.toFloat() / contentLength.toFloat())
                            }
                        }
                    }
                }
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            onProgress(1f)
            targetFile
        } catch (e: Exception) {
            tempFile.delete()
            null
        }
    }

    fun deleteModel(model: GemmaModel): Boolean {
        val file = getModelFile(model)
        if (initializedModelPath == file.absolutePath) release()
        return !file.exists() || file.delete()
    }

    fun release() {
        releaseLocked()
    }

    private fun releaseLocked() {
        engine?.close()
        engine = null
        initializedModelPath = null
    }
}
