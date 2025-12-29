package com.path.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.path.app.data.preferences.UserPreferences
import com.path.app.data.repository.PathRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class StreakStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalChaptersRead: Int,
    val totalBooksCompleted: Int,
    val lastStudyDate: String,
    val studyPercentage: Float
)

class StreakViewModel(
    private val userPreferences: UserPreferences,
    private val pathRepository: PathRepository,
    private val context: Context
) : ViewModel() {

    var stats by mutableStateOf(
        StreakStats(
            currentStreak = 0,
            longestStreak = 0,
            totalChaptersRead = 0,
            totalBooksCompleted = 0,
            lastStudyDate = "Never",
            studyPercentage = 0f
        )
    )
        private set

    var isLoading by mutableStateOf(true)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            isLoading = true
            try {
                val streak = userPreferences.streak.first()
                val lastStudyTimestamp = userPreferences.lastStudyDate.first()

                // Get total chapters read
                val allProgress = pathRepository.getAllProgress().first()
                val chaptersRead = allProgress.count { it.isCompleted }

                // Count unique books
                val uniqueBooks = allProgress.filter { it.isCompleted }
                    .map { it.bookName }
                    .distinct()
                    .size

                // Format last study date
                val lastStudyDate = if (lastStudyTimestamp > 0) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(java.util.Date(lastStudyTimestamp))
                } else {
                    "Never"
                }

                // Calculate percentage (assume 1189 total chapters in Bible)
                val totalBibleChapters = 1189
                val percentage = (chaptersRead.toFloat() / totalBibleChapters * 100).coerceAtMost(100f)

                stats = StreakStats(
                    currentStreak = streak,
                    longestStreak = streak, // In future, track separately
                    totalChaptersRead = chaptersRead,
                    totalBooksCompleted = uniqueBooks,
                    lastStudyDate = lastStudyDate,
                    studyPercentage = percentage
                )
            } catch (e: Exception) {
                error = "Failed to load stats: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun shareStreak() {
        val shareText = buildString {
            appendLine("📖 My Path Bible Study Progress")
            appendLine()
            appendLine("🔥 ${stats.currentStreak} day streak")
            appendLine("📚 ${stats.totalChaptersRead} chapters read")
            appendLine("✅ ${stats.totalBooksCompleted} books completed")
            appendLine()
            appendLine("Join me in daily Bible study! 🙏")
        }

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(intent, "Share your progress")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun refresh() {
        loadStats()
    }
}
