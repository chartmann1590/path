package com.path.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.path.app.data.local.entity.FavoriteEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToVerse: (String, Int) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    var showActionSheet by remember { mutableStateOf<FavoriteEntity?>(null) }

    // AI Summary Dialog
    if (viewModel.selectedVerse != null) {
        AiSummaryDialog(
            title = "${viewModel.selectedVerse!!.bookName} ${viewModel.selectedVerse!!.chapter}:${viewModel.selectedVerse!!.verseNumber}",
            summary = viewModel.aiSummary ?: "",
            isLoading = viewModel.isGeneratingAi,
            onDismiss = { viewModel.dismissAiSummary() }
        )
    }

    // Action Bottom Sheet
    if (showActionSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "${showActionSheet!!.bookName} ${showActionSheet!!.chapter}:${showActionSheet!!.verseNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ListItem(
                    headlineContent = { Text("Share Verse") },
                    leadingContent = {
                        Icon(Icons.Default.Share, contentDescription = "Share verse")
                    },
                    modifier = Modifier
                        .clickable {
                            viewModel.shareVerse(showActionSheet!!)
                            showActionSheet = null
                        }
                        .semantics { contentDescription = "Share verse ${showActionSheet!!.bookName} ${showActionSheet!!.chapter}:${showActionSheet!!.verseNumber}" }
                )

                if (viewModel.aiEnabled) {
                ListItem(
                    headlineContent = { Text("AI Summary") },
                    leadingContent = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI summary")
                    },
                    modifier = Modifier
                        .clickable {
                            viewModel.generateAiSummary(showActionSheet!!)
                            showActionSheet = null
                        }
                        .semantics { contentDescription = "Generate AI summary for ${showActionSheet!!.bookName} ${showActionSheet!!.chapter}:${showActionSheet!!.verseNumber}" }
                )
                }

                ListItem(
                    headlineContent = { Text("Remove from Favorites") },
                    leadingContent = {
                        Icon(Icons.Default.Delete, contentDescription = "Remove from favorites", tint = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier
                        .clickable {
                            viewModel.removeFavorite(showActionSheet!!.verseId)
                            showActionSheet = null
                        }
                        .semantics { contentDescription = "Remove ${showActionSheet!!.bookName} ${showActionSheet!!.chapter}:${showActionSheet!!.verseNumber} from favorites" }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Favorite Verses")
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No favorite verses yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Save verses from search or while reading",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites, key = { it.verseId }) { favorite ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    onNavigateToVerse(favorite.bookName, favorite.chapter)
                                },
                                onLongClick = {
                                    showActionSheet = favorite
                                }
                            )
                            .semantics { contentDescription = "Favorite verse ${favorite.bookName} ${favorite.chapter}:${favorite.verseNumber}. Tap to read, long press for options" }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${favorite.bookName} ${favorite.chapter}:${favorite.verseNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"${favorite.verseText}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.removeFavorite(favorite.verseId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove from favorites",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
