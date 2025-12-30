package com.biblereadingpath.app.data.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.biblereadingpath.app.data.local.PathDatabase
import com.biblereadingpath.app.data.local.entity.FavoriteEntity
import com.biblereadingpath.app.data.local.entity.NoteEntity
import com.biblereadingpath.app.data.local.entity.ProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

data class BackupData(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val progress: List<ProgressEntity>,
    val notes: List<NoteEntity>,
    val favorites: List<FavoriteEntity>,
    val streak: Int,
    val longestStreak: Int,
    val lastStudyDate: Long
)

class BackupManager(
    private val context: Context,
    private val database: PathDatabase
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun createBackup(
        outputStream: OutputStream,
        streak: Int,
        longestStreak: Int,
        lastStudyDate: Long
    ) = withContext(Dispatchers.IO) {
        try {
            // Gather all data
            val progressDao = database.progressDao()
            val notesDao = database.noteDao()
            val favoritesDao = database.favoriteDao()

            val progress = progressDao.getAllProgress().first()
            val notes = notesDao.getAllNotes().first()
            val favorites = favoritesDao.getAllFavorites().first()

            val backupData = BackupData(
                progress = progress,
                notes = notes,
                favorites = favorites,
                streak = streak,
                longestStreak = longestStreak,
                lastStudyDate = lastStudyDate
            )

            // Write to output stream
            val json = gson.toJson(backupData)
            outputStream.write(json.toByteArray())
            outputStream.flush()
            outputStream.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure<Unit>(e)
        }
    }

    suspend fun restoreBackup(inputStream: java.io.InputStream): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            // Read and parse JSON
            val json = inputStream.bufferedReader().use { it.readText() }
            val backupData = gson.fromJson(json, BackupData::class.java)

            // Restore data to database
            val progressDao = database.progressDao()
            val notesDao = database.noteDao()
            val favoritesDao = database.favoriteDao()

            // Insert all progress
            backupData.progress.forEach { progressDao.updateProgress(it) }

            // Insert all notes
            backupData.notes.forEach { notesDao.insertNote(it) }

            // Insert all favorites
            backupData.favorites.forEach { favoritesDao.insertFavorite(it) }

            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateBackupFilename(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = dateFormat.format(Date())
        return "path_backup_$timestamp.json"
    }
}
