package com.biblereadingpath.app.data.remote

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OnDeviceLlmService(
    private val context: Context
) {
    enum class ModelStatus {
        NOT_DOWNLOADED,
        DOWNLOADING,
        READY,
        ERROR
    }

    data class ModelState(
        val status: ModelStatus,
        val modelName: String = "",
        val progress: Float = 0f,
        val error: String? = null
    )

    private var isInitialized = false
    private var modelPath: String? = null

    fun isReady(): Boolean = isInitialized && modelPath != null

    fun getModelDirectory(): File {
        return File(context.filesDir, "ai_models").also { it.mkdirs() }
    }

    fun getDownloadedModels(): List<String> {
        val dir = getModelDirectory()
        return dir.listFiles()
            ?.filter { it.extension == "task" || it.extension == "tflite" || it.name.contains("gemma") }
            ?.map { it.name }
            ?: emptyList()
    }

    suspend fun initialize(modelFile: File): Boolean = withContext(Dispatchers.IO) {
        if (!modelFile.exists()) {
            isInitialized = false
            modelPath = null
            return@withContext false
        }

        try {
            modelPath = modelFile.absolutePath
            isInitialized = true
            true
        } catch (e: Exception) {
            isInitialized = false
            modelPath = null
            false
        }
    }

    suspend fun generate(prompt: String): String? = withContext(Dispatchers.IO) {
        if (!isReady()) return@withContext null

        try {
            generateInternal(prompt)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateInternal(prompt: String): String? {
        return null
    }

    suspend fun downloadModel(
        modelUrl: String,
        modelName: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val dir = getModelDirectory()
            val targetFile = File(dir, modelName)

            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder().url(modelUrl).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body ?: return@withContext null
            val contentLength = responseBody.contentLength()

            responseBody.byteStream().use { input ->
                targetFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
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

            targetFile
        } catch (e: Exception) {
            null
        }
    }

    fun release() {
        isInitialized = false
        modelPath = null
    }
}
