package com.biblereadingpath.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    companion object {
        val BIBLE_TRANSLATION = stringPreferencesKey("bible_translation")
        val DAILY_PACE = intPreferencesKey("daily_pace")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val LAST_STUDY_DATE = longPreferencesKey("last_study_date")
        val OLLAMA_URL = stringPreferencesKey("ollama_url")
        val OLLAMA_MODEL = stringPreferencesKey("ollama_model")
        val AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val GEMMA_MODEL = stringPreferencesKey("gemma_model")
        val GEMMA_MODEL_PATH = stringPreferencesKey("gemma_model_path")
        
        // Verse of the Day Cache for Widgets
        val VOD_TEXT = stringPreferencesKey("vod_text")
        val VOD_REF = stringPreferencesKey("vod_ref")
        val VOD_DATE = longPreferencesKey("vod_date") // Days since epoch

        // TTS
        val TTS_VOICE_NAME = stringPreferencesKey("tts_voice_name")

        // Reminders
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_START_HOUR = intPreferencesKey("reminder_start_hour")
        val REMINDER_END_HOUR = intPreferencesKey("reminder_end_hour")
        
        // Current study position
        val CURRENT_BOOK = stringPreferencesKey("current_book")
        val CURRENT_CHAPTER = intPreferencesKey("current_chapter")

        // Ad Credits & Ad-Free Status
        val AD_CREDITS = intPreferencesKey("ad_credits")
        val AD_FREE_UNTIL = longPreferencesKey("ad_free_until") // Timestamp in millis

        // Onboarding & Tutorial
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val HOME_TUTORIAL_SHOWN = booleanPreferencesKey("home_tutorial_shown")
        val READER_THEME = stringPreferencesKey("reader_theme")

        val STUDY_PLAN_TYPE = stringPreferencesKey("study_plan_type")
        val STUDY_PLAN_ID = stringPreferencesKey("study_plan_id")
    }

    val translation: Flow<String> = context.dataStore.data.map { it[BIBLE_TRANSLATION] ?: "web" }
    val pace: Flow<Int> = context.dataStore.data.map { it[DAILY_PACE] ?: 1 }
    val streak: Flow<Int> = context.dataStore.data.map { it[STREAK_COUNT] ?: 0 }
    val lastStudyDate: Flow<Long> = context.dataStore.data.map { it[LAST_STUDY_DATE] ?: 0L }
    val aiEnabled: Flow<Boolean> = context.dataStore.data.map { it[AI_ENABLED] ?: false }
    val aiProvider: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AI_PROVIDER] ?: if (preferences[AI_ENABLED] == true) "OLLAMA" else "OFF"
    }
    val ollamaUrl: Flow<String?> = context.dataStore.data.map { it[OLLAMA_URL] }
    val ollamaModel: Flow<String?> = context.dataStore.data.map { it[OLLAMA_MODEL] }
    val gemmaModel: Flow<String> = context.dataStore.data.map { it[GEMMA_MODEL] ?: "E2B" }
    val gemmaModelPath: Flow<String?> = context.dataStore.data.map { it[GEMMA_MODEL_PATH] }

    val vodText: Flow<String?> = context.dataStore.data.map { it[VOD_TEXT] }
    val vodRef: Flow<String?> = context.dataStore.data.map { it[VOD_REF] }
    val vodDate: Flow<Long> = context.dataStore.data.map { it[VOD_DATE] ?: 0L }
    val ttsVoiceName: Flow<String?> = context.dataStore.data.map { it[TTS_VOICE_NAME] }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDERS_ENABLED] ?: false }
    val reminderStartHour: Flow<Int> = context.dataStore.data.map { it[REMINDER_START_HOUR] ?: 9 }
    val reminderEndHour: Flow<Int> = context.dataStore.data.map { it[REMINDER_END_HOUR] ?: 21 }
    
    val currentBook: Flow<String?> = context.dataStore.data.map { it[CURRENT_BOOK] }
    val currentChapter: Flow<Int> = context.dataStore.data.map { it[CURRENT_CHAPTER] ?: 1 }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val tutorialCompleted: Flow<Boolean> = context.dataStore.data.map { it[TUTORIAL_COMPLETED] ?: false }
    val homeTutorialShown: Flow<Boolean> = context.dataStore.data.map { it[HOME_TUTORIAL_SHOWN] ?: false }

    val readerTheme: Flow<String> = context.dataStore.data.map { it[READER_THEME] ?: "LIGHT" }

    suspend fun updateStreak(newStreak: Int) {
        context.dataStore.edit { it[STREAK_COUNT] = newStreak }
    }
    
    suspend fun saveTtsVoice(voiceName: String) {
        context.dataStore.edit { it[TTS_VOICE_NAME] = voiceName }
    }
    
    suspend fun saveVerseOfTheDay(text: String, reference: String, date: Long? = null) {
        context.dataStore.edit { 
            it[VOD_TEXT] = text
            it[VOD_REF] = reference
            if (date != null) {
                it[VOD_DATE] = date
            }
        }
    }
    
    suspend fun markStudyComplete() {
        context.dataStore.edit { preferences ->
            val lastDate = preferences[LAST_STUDY_DATE] ?: 0L
            val currentStreak = preferences[STREAK_COUNT] ?: 0
            
            val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24) // Days since epoch
            val lastDay = lastDate / (1000 * 60 * 60 * 24)
            
            val newStreak = when {
                today == lastDay -> currentStreak // Already done today
                today == lastDay + 1 -> currentStreak + 1 // Consecutive day
                else -> 1 // Broken streak
            }
            
            preferences[STREAK_COUNT] = newStreak
            preferences[LAST_STUDY_DATE] = System.currentTimeMillis()
        }
    }

    suspend fun setAiEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[AI_ENABLED] = enabled
            it[AI_PROVIDER] = if (enabled) "OLLAMA" else "OFF"
        }
    }

    suspend fun setAiProvider(provider: String) {
        context.dataStore.edit {
            it[AI_PROVIDER] = provider
            it[AI_ENABLED] = provider != "OFF"
        }
    }

    suspend fun setGemmaModel(model: String) {
        context.dataStore.edit { it[GEMMA_MODEL] = model }
    }

    suspend fun setGemmaModelPath(path: String?) {
        context.dataStore.edit {
            if (path == null) {
                it.remove(GEMMA_MODEL_PATH)
            } else {
                it[GEMMA_MODEL_PATH] = path
            }
        }
    }
    
    suspend fun setOllamaConfig(url: String, model: String) {
        context.dataStore.edit {
            it[OLLAMA_URL] = url
            it[OLLAMA_MODEL] = model
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDERS_ENABLED] = enabled }
    }

    suspend fun setReminderTimeRange(startHour: Int, endHour: Int) {
        context.dataStore.edit {
            it[REMINDER_START_HOUR] = startHour
            it[REMINDER_END_HOUR] = endHour
        }
    }
    
    suspend fun setCurrentStudyPosition(book: String, chapter: Int) {
        context.dataStore.edit {
            it[CURRENT_BOOK] = book
            it[CURRENT_CHAPTER] = chapter
        }
    }

    // Ad Credits & Ad-Free Management
    val adCredits: Flow<Int> = context.dataStore.data.map { it[AD_CREDITS] ?: 0 }
    val adFreeUntil: Flow<Long> = context.dataStore.data.map { it[AD_FREE_UNTIL] ?: 0L }

    suspend fun addCredits(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[AD_CREDITS] ?: 0
            preferences[AD_CREDITS] = current + amount
        }
    }

    suspend fun spendCredits(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val current = preferences[AD_CREDITS] ?: 0
            if (current >= amount) {
                preferences[AD_CREDITS] = current - amount
                success = true
            }
        }
        return success
    }

    suspend fun setAdFreeUntil(timestampMillis: Long) {
        context.dataStore.edit { it[AD_FREE_UNTIL] = timestampMillis }
    }

    suspend fun isAdFree(): Boolean {
        val adFreeUntil = context.dataStore.data.map { it[AD_FREE_UNTIL] ?: 0L }
        val expiryTime = adFreeUntil.map { it }.first()
        return System.currentTimeMillis() < expiryTime
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { it[TUTORIAL_COMPLETED] = completed }
    }

    suspend fun setHomeTutorialShown(shown: Boolean) {
        context.dataStore.edit { it[HOME_TUTORIAL_SHOWN] = shown }
    }

    val studyPlanType: Flow<String> = context.dataStore.data.map { it[STUDY_PLAN_TYPE] ?: "sequential" }
    val studyPlanId: Flow<String?> = context.dataStore.data.map { it[STUDY_PLAN_ID] }

    suspend fun setStudyPlan(typeKey: String, id: String? = null) {
        context.dataStore.edit {
            it[STUDY_PLAN_TYPE] = typeKey
            if (id != null) it[STUDY_PLAN_ID] = id else it.remove(STUDY_PLAN_ID)
        }
    }

    suspend fun setReaderTheme(theme: String) {
        context.dataStore.edit { it[READER_THEME] = theme }
    }

        suspend fun setTranslation(translation: String) {
        context.dataStore.edit { it[BIBLE_TRANSLATION] = translation.lowercase() }
    }
}

