package com.biblereadingpath.app.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * Centralized Firebase manager for Analytics and Crashlytics
 */
class FirebaseManager(context: Context) {

    private val analytics: FirebaseAnalytics = Firebase.analytics
    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics

    init {
        // Enable analytics collection
        analytics.setAnalyticsCollectionEnabled(true)

        // Enable crashlytics collection
        crashlytics.setCrashlyticsCollectionEnabled(true)
    }

    // ============ Analytics Events ============

    /**
     * Track when a user reads a chapter
     */
    fun logChapterRead(book: String, chapter: Int) {
        val params = Bundle().apply {
            putString("book_name", book)
            putInt("chapter_number", chapter)
        }
        analytics.logEvent("chapter_read", params)
    }

    /**
     * Track when a user completes a chapter
     */
    fun logChapterCompleted(book: String, chapter: Int) {
        val params = Bundle().apply {
            putString("book_name", book)
            putInt("chapter_number", chapter)
        }
        analytics.logEvent("chapter_completed", params)
    }

    /**
     * Track search queries
     */
    fun logSearch(query: String, resultCount: Int) {
        val params = Bundle().apply {
            putString("search_query", query)
            putInt("result_count", resultCount)
        }
        analytics.logEvent("search", params)
    }

    /**
     * Track when a user adds a favorite
     */
    fun logFavoriteAdded(book: String, chapter: Int, verse: Int) {
        val params = Bundle().apply {
            putString("book_name", book)
            putInt("chapter_number", chapter)
            putInt("verse_number", verse)
        }
        analytics.logEvent("favorite_added", params)
    }

    /**
     * Track when a user removes a favorite
     */
    fun logFavoriteRemoved(book: String, chapter: Int, verse: Int) {
        val params = Bundle().apply {
            putString("book_name", book)
            putInt("chapter_number", chapter)
            putInt("verse_number", verse)
        }
        analytics.logEvent("favorite_removed", params)
    }

    /**
     * Track when a user shares a verse
     */
    fun logVerseShared(book: String, chapter: Int, verse: Int) {
        val params = Bundle().apply {
            putString("book_name", book)
            putInt("chapter_number", chapter)
            putInt("verse_number", verse)
        }
        analytics.logEvent("verse_shared", params)
    }

    /**
     * Track when a user creates a note
     */
    fun logNoteCreated(book: String, chapter: Int) {
        val params = Bundle().apply {
            putString("book_name", book)
            putInt("chapter_number", chapter)
        }
        analytics.logEvent("note_created", params)
    }

    /**
     * Track AI summary generation
     */
    fun logAiSummaryGenerated(type: String, book: String, chapter: Int? = null) {
        val params = Bundle().apply {
            putString("summary_type", type) // "book" or "chapter" or "verse"
            putString("book_name", book)
            chapter?.let { putInt("chapter_number", it) }
        }
        analytics.logEvent("ai_summary_generated", params)
    }

    /**
     * Track AI configuration
     */
    fun logAiEnabled(enabled: Boolean) {
        val params = Bundle().apply {
            putBoolean("ai_enabled", enabled)
        }
        analytics.logEvent("ai_settings_changed", params)
    }

    /**
     * Track TTS usage
     */
    fun logTtsUsed(book: String, chapter: Int) {
        val params = Bundle().apply {
            putString("book_name", book)
            putInt("chapter_number", chapter)
        }
        analytics.logEvent("tts_used", params)
    }

    /**
     * Track streak milestones
     */
    fun logStreakMilestone(streakDays: Int) {
        val params = Bundle().apply {
            putInt("streak_days", streakDays)
        }
        analytics.logEvent("streak_milestone", params)
    }

    /**
     * Track reminders
     */
    fun logRemindersEnabled(enabled: Boolean) {
        val params = Bundle().apply {
            putBoolean("reminders_enabled", enabled)
        }
        analytics.logEvent("reminders_settings_changed", params)
    }

    /**
     * Track translation changes
     */
    fun logTranslationChanged(translation: String) {
        val params = Bundle().apply {
            putString("translation", translation)
        }
        analytics.logEvent("translation_changed", params)
    }

    /**
     * Track backup creation
     */
    fun logBackupCreated() {
        analytics.logEvent("backup_created", null)
    }

    /**
     * Track backup restoration
     */
    fun logBackupRestored(progressCount: Int, notesCount: Int, favoritesCount: Int) {
        val params = Bundle().apply {
            putInt("progress_count", progressCount)
            putInt("notes_count", notesCount)
            putInt("favorites_count", favoritesCount)
        }
        analytics.logEvent("backup_restored", params)
    }

    /**
     * Track downloads
     */
    fun logDownloadStarted(book: String) {
        val params = Bundle().apply {
            putString("book_name", book)
        }
        analytics.logEvent("download_started", params)
    }

    fun logDownloadCompleted(book: String) {
        val params = Bundle().apply {
            putString("book_name", book)
        }
        analytics.logEvent("download_completed", params)
    }

    /**
     * Track screen views
     */
    fun logScreenView(screenName: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    /**
     * Track roadmap view
     */
    fun logRoadmapViewed() {
        analytics.logEvent("roadmap_viewed", null)
    }

    // ============ Crashlytics ============

    /**
     * Set user identifier for crash reports
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Log a non-fatal exception
     */
    fun logException(exception: Throwable) {
        crashlytics.recordException(exception)
    }

    /**
     * Log a custom key-value pair for crash reports
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Log a custom message for debugging
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Force a crash (for testing only - remove in production)
     */
    fun forceCrash() {
        throw RuntimeException("Test crash from FirebaseManager")
    }
}
