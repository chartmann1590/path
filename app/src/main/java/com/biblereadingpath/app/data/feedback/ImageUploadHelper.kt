package com.biblereadingpath.app.data.feedback

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUploadHelper {
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.use { stream ->
                val buffer = ByteArrayOutputStream()
                stream.copyTo(buffer)
                buffer.toByteArray()
            }
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun generateAssetFilename(issueNumber: Int?): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US)
            .format(java.util.Date())
        val random = (1000..9999).random()
        val prefix = issueNumber?.let { "issue-$it-" } ?: "feedback-"
        return "$prefix$timestamp-$random.png"
    }
}
