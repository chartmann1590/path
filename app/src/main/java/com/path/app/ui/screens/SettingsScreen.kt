package com.path.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToAbout: () -> Unit = {}
) {
    val context = LocalContext.current
    val aiEnabled by viewModel.aiEnabled.collectAsState()
    val ollamaUrl by viewModel.ollamaUrlInput.collectAsState()
    val ollamaModel by viewModel.ollamaModel.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val isLoadingModels by viewModel.isLoadingModels.collectAsState()
    val modelFetchError by viewModel.modelFetchError.collectAsState()
    val urlValidationError by viewModel.urlValidationError.collectAsState()

    val voices by viewModel.ttsVoices.collectAsState()
    val currentVoiceName by viewModel.currentVoice.collectAsState()

    val remindersEnabled by viewModel.remindersEnabled.collectAsState()
    val reminderStartHour by viewModel.reminderStartHour.collectAsState()
    val reminderEndHour by viewModel.reminderEndHour.collectAsState()

    val backupStatus by viewModel.backupStatus.collectAsState()
    val restoreStatus by viewModel.restoreStatus.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showBackupInstructions by remember { mutableStateOf(false) }

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setRemindersEnabled(true)
        } else {
            showPermissionDialog = true
        }
    }

    // Backup file picker
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.createBackup(it) }
    }

    // Restore file picker
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreBackup(it) }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Pre-Android 13 doesn't need runtime permission
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Notification Permission Required") },
            text = { Text("To receive study reminders, please enable notifications in your device settings.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showBackupInstructions) {
        AlertDialog(
            onDismissRequest = { showBackupInstructions = false },
            title = { Text("Backup & Restore Instructions") },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text("📤 Create Backup:", fontWeight = FontWeight.Bold)
                    Text("• Tap 'Create Backup' below")
                    Text("• Choose where to save the file")
                    Text("• Your progress, notes, favorites, and streak will be saved")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📥 Restore Backup:", fontWeight = FontWeight.Bold)
                    Text("• Tap 'Restore Backup' below")
                    Text("• Select your backup file (path_backup_*.json)")
                    Text("• Your data will be imported")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("💡 Tips:", fontWeight = FontWeight.Bold)
                    Text("• Save backup files to cloud storage (Google Drive, Dropbox)")
                    Text("• Create backups regularly")
                    Text("• Transfer files to new devices before restoring")
                    Text("• Restoring will merge with existing data, not replace it")
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupInstructions = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        if (modelFetchError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    text = modelFetchError!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        if (urlValidationError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    text = urlValidationError!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        ListItem(
            headlineContent = { Text("AI Insights (Ollama)") },
            supportingContent = { Text("Get AI-generated summaries and explanations.") },
            trailingContent = {
                Switch(
                    checked = aiEnabled,
                    onCheckedChange = { viewModel.setAiEnabled(it) }
                )
            }
        )
        
        if (aiEnabled) {
            OutlinedTextField(
                value = ollamaUrl,
                onValueChange = { viewModel.setOllamaUrl(it) },
                label = { Text("Ollama URL (e.g. http://192.168.1.5:11434)") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = urlValidationError != null,
                supportingText = if (urlValidationError != null) {
                    { Text(urlValidationError!!, color = MaterialTheme.colorScheme.error) }
                } else null,
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.fetchModels() },
                        modifier = Modifier.semantics { contentDescription = "Refresh available AI models" }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Models")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = ollamaModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Model Name") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (isLoadingModels) {
                        DropdownMenuItem(
                            text = { Text("Loading...") },
                            onClick = { }
                        )
                    } else if (availableModels.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No models found. Check URL.") },
                            onClick = { viewModel.fetchModels() }
                        )
                    } else {
                        availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = { 
                                    viewModel.setOllamaModel(model)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text(
            text = "Voice & Audio",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (voices.isEmpty()) {
            Text("Loading voices...", style = MaterialTheme.typography.bodyMedium)
        } else {
            // Simple list of voices - expanding this could be better but list is fine for v1
            var expanded by remember { mutableStateOf(false) }
            
            ListItem(
                headlineContent = { Text("Reader Voice") },
                supportingContent = { Text(currentVoiceName ?: "Default") },
                modifier = Modifier.clickable { expanded = !expanded }
            )
            
            if (expanded) {
                Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    LazyColumn {
                        items(voices) { voice ->
                            ListItem(
                                headlineContent = { Text(voice.name) },
                                leadingContent = {
                                    RadioButton(
                                        selected = voice.name == currentVoiceName,
                                        onClick = { viewModel.setVoice(voice) }
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = { viewModel.previewVoice(voice) },
                                        modifier = Modifier.semantics { contentDescription = "Preview voice ${voice.name}" }
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Preview")
                                    }
                                },
                                modifier = Modifier.clickable { viewModel.setVoice(voice) }
                            )
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Reminders",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ListItem(
            headlineContent = { Text("Study Reminders") },
            supportingContent = { Text("Get random daily reminders to study") },
            trailingContent = {
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Check if we have permission
                            if (hasNotificationPermission()) {
                                viewModel.setRemindersEnabled(true)
                            } else {
                                // Request permission
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setRemindersEnabled(true)
                                }
                            }
                        } else {
                            viewModel.setRemindersEnabled(false)
                        }
                    }
                )
            }
        )

        if (remindersEnabled) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Active hours: ${reminderStartHour}:00 - ${reminderEndHour}:00",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Start Hour: $reminderStartHour:00",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = reminderStartHour.toFloat(),
                    onValueChange = { viewModel.setReminderStartHour(it.toInt()) },
                    valueRange = 0f..23f,
                    steps = 22
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "End Hour: $reminderEndHour:00",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = reminderEndHour.toFloat(),
                    onValueChange = { viewModel.setReminderEndHour(it.toInt()) },
                    valueRange = 0f..23f,
                    steps = 22
                )
                Text(
                    text = "You'll receive one random reminder per day during these hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextButton(onClick = { showBackupInstructions = true }) {
                Text("How it works")
            }
        }

        Text(
            text = "Save your progress to move between devices",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { createBackupLauncher.launch(viewModel.getBackupFilename()) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share backup", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Backup")
            }
            OutlinedButton(
                onClick = { restoreBackupLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Restore backup", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restore Backup")
            }
        }

        if (backupStatus != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (backupStatus!!.contains("success", ignoreCase = true))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = backupStatus!!,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (restoreStatus != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (restoreStatus!!.contains("completed", ignoreCase = true))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = restoreStatus!!,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        ListItem(
            headlineContent = { Text("Bible Translation") },
            trailingContent = { Text("WEB") }
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        ListItem(
            headlineContent = { Text("About") },
            supportingContent = { Text("App information and GitHub repository") },
            modifier = Modifier.clickable {
                onNavigateToAbout()
            }
        )
    }
}
