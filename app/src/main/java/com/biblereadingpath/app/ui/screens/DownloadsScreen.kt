package com.biblereadingpath.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.biblereadingpath.app.data.BibleTranslations
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    onNavigateBack: () -> Unit
) {
    // Show error snackbar if there's an error
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Download for Offline") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.downloadAll() },
                        modifier = Modifier.semantics { contentDescription = "Download all Bible books for offline reading" }
                    ) {
                        Text("Download All")
                    }
                }
            )
        }
    ) { padding ->
        val currentTranslationId by viewModel.currentTranslation.collectAsState()
        val currentTranslation = BibleTranslations.getTranslationById(currentTranslationId)
            ?: BibleTranslations.getDefault()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Info card with disclaimer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Offline Reading",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Download Bible books to read without internet connection.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "About Downloads:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Uses ${currentTranslation.displayName} translation\n• Books shown below are verified available from our Bible API\n• Downloads are saved to your device for offline reading\n• Chapter counts match the actual available content\n• Downloads take time due to API rate limits (please be patient)",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Statistics
            val downloadedCount = viewModel.bibleBooks.count { it.isDownloaded }
            val totalCount = viewModel.bibleBooks.size
            val downloadingCount = viewModel.bibleBooks.count { it.isDownloading }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$downloadedCount of $totalCount books downloaded",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                if (downloadingCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Downloading $downloadingCount books...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (viewModel.downloadStats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.downloadStats,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Book list with section headers
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Old Testament
                item {
                    Text(
                        text = "Old Testament",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(viewModel.bibleBooks.filter { it.testament == com.biblereadingpath.app.data.BibleBooks.Testament.OLD }) { book ->
                    BookDownloadItem(
                        book = book,
                        onDownload = { viewModel.downloadBook(book.name) }
                    )
                    Divider()
                }

                // New Testament
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "New Testament",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(viewModel.bibleBooks.filter { it.testament == com.biblereadingpath.app.data.BibleBooks.Testament.NEW }) { book ->
                    BookDownloadItem(
                        book = book,
                        onDownload = { viewModel.downloadBook(book.name) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun BookDownloadItem(
    book: BibleBook,
    onDownload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Show download status
            val chapterText = if (book.downloadedChapters > 0) {
                if (book.isDownloaded) {
                    "${book.chapters} chapters (Complete)"
                } else {
                    "${book.downloadedChapters}/${book.chapters} chapters (Partial)"
                }
            } else {
                "${book.chapters} chapters"
            }

            Text(
                text = chapterText,
                style = MaterialTheme.typography.bodySmall,
                color = if (book.downloadedChapters > 0 && !book.isDownloaded) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // Progress bar
            if (book.isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = book.downloadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(book.downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Download button/status
        if (book.isDownloaded) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Fully Downloaded",
                tint = MaterialTheme.colorScheme.primary
            )
        } else if (book.isDownloading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            // Show download button for both new downloads and resuming partial downloads
            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = if (book.downloadedChapters > 0) {
                        "Resume Download ${book.name}"
                    } else {
                        "Download ${book.name}"
                    },
                    tint = if (book.downloadedChapters > 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
