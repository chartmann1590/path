package com.path.app.ui.screens

import android.content.Context
import android.net.Uri
import android.speech.tts.Voice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.path.app.analytics.FirebaseManager
import com.path.app.data.backup.BackupManager
import com.path.app.data.local.PathDatabase
import com.path.app.data.preferences.UserPreferences
import com.path.app.data.repository.OllamaRepository
import com.path.app.ui.components.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.room.Room

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val firebaseManager: FirebaseManager,
    private val context: Context // Need context for TTS and notifications
) : ViewModel() {
    private val ttsManager = TtsManager(context)
    private val ollamaRepository = OllamaRepository(userPreferences)

    private val database = Room.databaseBuilder(
        context,
        PathDatabase::class.java,
        "path.db"
    ).build()

    private val backupManager = BackupManager(context, database)

    val aiEnabled: StateFlow<Boolean> = userPreferences.aiEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    // Local state for UI to ensure smooth typing
    private val _ollamaUrlInput = MutableStateFlow("http://localhost:11434")
    val ollamaUrlInput = _ollamaUrlInput.asStateFlow()

    // Observe DataStore only for initial load or external changes
    init {
        viewModelScope.launch {
            userPreferences.ollamaUrl.first()?.let { savedUrl ->
                _ollamaUrlInput.value = savedUrl
            }
        }
    }

    val ollamaModel: StateFlow<String> = userPreferences.ollamaModel
        .map { it ?: "llama2" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "llama2")

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels = _availableModels.asStateFlow()
    
    private val _isLoadingModels = MutableStateFlow(false)
    val isLoadingModels = _isLoadingModels.asStateFlow()
    
    private val _modelFetchError = MutableStateFlow<String?>(null)
    val modelFetchError = _modelFetchError.asStateFlow()
    
    private val _urlValidationError = MutableStateFlow<String?>(null)
    val urlValidationError = _urlValidationError.asStateFlow()

    val ttsVoices = ttsManager.availableVoices
    
    val currentVoice: StateFlow<String?> = userPreferences.ttsVoiceName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setAiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAiEnabled(enabled)
            // Track AI settings change in Firebase
            firebaseManager.logAiEnabled(enabled)
            // Don't auto-fetch on enable if URL is likely localhost on device, let user refresh manually
        }
    }
    
    fun setOllamaUrl(url: String) {
        _ollamaUrlInput.value = url
        
        // Validate URL
        val validationResult = validateOllamaUrl(url)
        if (validationResult.isValid) {
            _urlValidationError.value = null
            viewModelScope.launch {
                userPreferences.setOllamaConfig(url, ollamaModel.value)
            }
        } else {
            _urlValidationError.value = validationResult.errorMessage
        }
    }
    
    private fun validateOllamaUrl(url: String): UrlValidationResult {
        if (url.isBlank()) {
            return UrlValidationResult(false, "URL cannot be empty")
        }
        
        val trimmed = url.trim()
        
        // Check if it starts with http:// or https://
        val hasProtocol = trimmed.startsWith("http://") || trimmed.startsWith("https://")
        val urlWithoutProtocol = if (hasProtocol) {
            trimmed.substringAfter("://")
        } else {
            trimmed
        }
        
        if (urlWithoutProtocol.isBlank()) {
            return UrlValidationResult(false, "URL must include a host address")
        }
        
        // Split host and port
        val parts = urlWithoutProtocol.split(":")
        val host = parts[0]
        val portStr = if (parts.size > 1) parts[1].split("/")[0] else null
        
        // Validate host
        if (host.isBlank()) {
            return UrlValidationResult(false, "Host cannot be empty")
        }
        
        // Check if host is a valid IP address or domain name
        val isValidHost = isValidIpAddress(host) || isValidDomainName(host) || host == "localhost"
        if (!isValidHost) {
            return UrlValidationResult(false, "Invalid host format. Use an IP address (e.g., 192.168.1.5) or domain name")
        }
        
        // Validate port if provided
        if (portStr != null && portStr.isNotBlank()) {
            try {
                val port = portStr.toInt()
                if (port < 1 || port > 65535) {
                    return UrlValidationResult(false, "Port must be between 1 and 65535")
                }
            } catch (e: NumberFormatException) {
                return UrlValidationResult(false, "Invalid port number")
            }
        }
        
        return UrlValidationResult(true, null)
    }
    
    private fun isValidIpAddress(host: String): Boolean {
        val parts = host.split(".")
        if (parts.size != 4) return false
        
        return parts.all { part ->
            try {
                val num = part.toInt()
                num in 0..255
            } catch (e: NumberFormatException) {
                false
            }
        }
    }
    
    private fun isValidDomainName(host: String): Boolean {
        // Basic domain validation: alphanumeric, dots, hyphens
        if (host.length > 253) return false
        if (host.startsWith(".") || host.endsWith(".")) return false
        if (host.startsWith("-") || host.endsWith("-")) return false
        
        val domainRegex = Regex("^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$")
        return domainRegex.matches(host)
    }
    
    private data class UrlValidationResult(val isValid: Boolean, val errorMessage: String?)
    
    fun setOllamaModel(model: String) {
        viewModelScope.launch {
            userPreferences.setOllamaConfig(_ollamaUrlInput.value, model)
        }
    }
    
    fun fetchModels() {
        viewModelScope.launch {
            _isLoadingModels.value = true
            _modelFetchError.value = null
            val url = _ollamaUrlInput.value
            try {
                val models = ollamaRepository.fetchAvailableModels(url)
                if (models.isEmpty()) {
                    _modelFetchError.value = "Connected to $url but found no models. Ensure you have pulled a model (e.g. 'ollama pull llama2')."
                } else {
                    _availableModels.value = models
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Ollama connection error to $url", e)
                val errorMsg = when {
                    e.message?.contains("EHOSTUNREACH") == true || e.message?.contains("No route to host") == true ->
                        "Cannot reach server at $url\n\n" +
                        "Remote server firewall is blocking port 11434.\n" +
                        "On your server, ensure:\n" +
                        "• Firewall allows inbound TCP 11434\n" +
                        "• Ollama running with OLLAMA_HOST=0.0.0.0\n" +
                        "• Server's external IP is correct"
                    e.message?.contains("timeout") == true ->
                        "Connection timeout to $url\n\nServer may be slow or unreachable."
                    e.message?.contains("refused") == true ->
                        "Connection refused at $url\n\nOllama may not be running on port 11434."
                    else ->
                        "Failed to connect to $url\nError: ${e.message}"
                }
                _modelFetchError.value = errorMsg
            }
            _isLoadingModels.value = false
        }
    }
    
    fun setVoice(voice: Voice) {
        viewModelScope.launch {
            userPreferences.saveTtsVoice(voice.name)
            ttsManager.setVoice(voice.name)
        }
    }
    
    fun previewVoice(voice: Voice) {
        ttsManager.setVoice(voice.name)
        ttsManager.speak("In the beginning God created the heaven and the earth.")
    }

    // Reminders
    val remindersEnabled: StateFlow<Boolean> = userPreferences.remindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderStartHour: StateFlow<Int> = userPreferences.reminderStartHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9)

    val reminderEndHour: StateFlow<Int> = userPreferences.reminderEndHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 21)

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setRemindersEnabled(enabled)
            // Track reminders settings change in Firebase
            firebaseManager.logRemindersEnabled(enabled)
            if (enabled) {
                // Schedule reminders
                val start = userPreferences.reminderStartHour.first()
                val end = userPreferences.reminderEndHour.first()
                com.path.app.notifications.ReminderScheduler.scheduleReminders(context, start, end)
            } else {
                // Cancel reminders
                com.path.app.notifications.ReminderScheduler.cancelReminders(context)
            }
        }
    }

    fun setReminderStartHour(hour: Int) {
        viewModelScope.launch {
            val end = userPreferences.reminderEndHour.first()
            userPreferences.setReminderTimeRange(hour, end)
            if (userPreferences.remindersEnabled.first()) {
                com.path.app.notifications.ReminderScheduler.scheduleReminders(context, hour, end)
            }
        }
    }

    fun setReminderEndHour(hour: Int) {
        viewModelScope.launch {
            val start = userPreferences.reminderStartHour.first()
            userPreferences.setReminderTimeRange(start, hour)
            if (userPreferences.remindersEnabled.first()) {
                com.path.app.notifications.ReminderScheduler.scheduleReminders(context, start, hour)
            }
        }
    }

    // Backup/Restore
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus = _backupStatus.asStateFlow()

    private val _restoreStatus = MutableStateFlow<String?>(null)
    val restoreStatus = _restoreStatus.asStateFlow()

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = "Creating backup..."
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    _backupStatus.value = "Error: Could not open file"
                    return@launch
                }

                val streak = userPreferences.streak.first()
                val lastStudyDate = userPreferences.lastStudyDate.first()

                val result = backupManager.createBackup(
                    outputStream,
                    streak,
                    streak, // Use current streak as longest for now
                    lastStudyDate
                )

                _backupStatus.value = if (result.isSuccess) {
                    // Track backup creation in Firebase
                    firebaseManager.logBackupCreated()
                    "Backup created successfully!"
                } else {
                    "Backup failed: ${result.exceptionOrNull()?.message}"
                }

                // Clear status after 3 seconds
                kotlinx.coroutines.delay(3000)
                _backupStatus.value = null
            } catch (e: Exception) {
                _backupStatus.value = "Error: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _backupStatus.value = null
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _restoreStatus.value = "Restoring backup..."
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _restoreStatus.value = "Error: Could not open file"
                    return@launch
                }

                val result = backupManager.restoreBackup(inputStream)

                if (result.isSuccess) {
                    val backupData = result.getOrNull()
                    if (backupData != null) {
                        // Restore preferences
                        userPreferences.updateStreak(backupData.streak)
                        // Note: We'd need to add a method to update lastStudyDate if needed

                        // Track backup restore in Firebase
                        firebaseManager.logBackupRestored(
                            backupData.progress.size,
                            backupData.notes.size,
                            backupData.favorites.size
                        )

                        _restoreStatus.value = "Restore completed! ${backupData.progress.size} chapters, ${backupData.notes.size} notes, ${backupData.favorites.size} favorites restored."
                    } else {
                        _restoreStatus.value = "Restore completed but data was empty"
                    }
                } else {
                    _restoreStatus.value = "Restore failed: ${result.exceptionOrNull()?.message}"
                }

                // Clear status after 5 seconds
                kotlinx.coroutines.delay(5000)
                _restoreStatus.value = null
            } catch (e: Exception) {
                _restoreStatus.value = "Error: ${e.message}"
                kotlinx.coroutines.delay(5000)
                _restoreStatus.value = null
            }
        }
    }

    fun getBackupFilename(): String = backupManager.generateBackupFilename()

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
