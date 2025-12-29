package com.path.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.path.app.ui.components.AdMobRewardedManager
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    viewModel: RewardsViewModel,
    rewardedAdManager: AdMobRewardedManager?
) {
    val credits by viewModel.credits.collectAsState()
    val adFreeUntil by viewModel.adFreeUntil.collectAsState()
    val isAdFree = viewModel.isAdFree()
    val remainingTime = viewModel.getRemainingAdFreeTime()

    // Show reward confirmation dialog
    if (viewModel.showRewardConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRewardConfirmation() },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Reward Earned!") },
            text = { Text("You've earned ${AdMobRewardedManager.CREDITS_PER_AD} credits!") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissRewardConfirmation() }) {
                    Text("Awesome!")
                }
            }
        )
    }

    // Show insufficient credits dialog
    if (viewModel.showInsufficientCreditsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissInsufficientCreditsDialog() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Insufficient Credits") },
            text = { Text("You don't have enough credits for this purchase. Watch more rewarded ads to earn credits!") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissInsufficientCreditsDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Icon(
            imageVector = Icons.Default.Stars,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Earn Credits",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Watch ads to remove interstitial ads",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Credits Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Credits",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$credits",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (isAdFree) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ad-Free Active!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRemainingTime(remainingTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Watch Ad Button
        Button(
            onClick = {
                val shown = rewardedAdManager?.showAd()
                if (shown == false) {
                    // Ad not ready yet
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = rewardedAdManager?.isAdReady() ?: false
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (rewardedAdManager?.isAdReady() == true)
                    "Watch Ad (+${AdMobRewardedManager.CREDITS_PER_AD} Credits)"
                else
                    "Loading Ad...",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // How It Works Section
        Text(
            text = "How Credits Work",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            icon = Icons.Default.PlayArrow,
            title = "Watch Rewarded Ads",
            description = "Each ad you watch earns you ${AdMobRewardedManager.CREDITS_PER_AD} credits"
        )
        Spacer(modifier = Modifier.height(8.dp))

        InfoCard(
            icon = Icons.Default.Star,
            title = "Earn Credits",
            description = "Credits accumulate and never expire"
        )
        Spacer(modifier = Modifier.height(8.dp))

        InfoCard(
            icon = Icons.Default.Block,
            title = "Remove Interstitial Ads",
            description = "Spend credits to disable full-screen ads for a period of time"
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // Purchase Options
        Text(
            text = "Remove Interstitial Ads",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        PurchaseOption(
            hours = 1,
            cost = 30,
            currentCredits = credits,
            isProcessing = viewModel.isProcessing,
            onPurchase = { viewModel.purchaseAdFreeTime(1, 30) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        PurchaseOption(
            hours = 3,
            cost = 80,
            currentCredits = credits,
            isProcessing = viewModel.isProcessing,
            onPurchase = { viewModel.purchaseAdFreeTime(3, 80) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        PurchaseOption(
            hours = 6,
            cost = 150,
            currentCredits = credits,
            isProcessing = viewModel.isProcessing,
            onPurchase = { viewModel.purchaseAdFreeTime(6, 150) },
            recommended = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Note
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Note: Banner ads will still appear at the bottom of screens. Only full-screen interstitial ads will be removed during ad-free periods.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PurchaseOption(
    hours: Int,
    cost: Int,
    currentCredits: Int,
    isProcessing: Boolean,
    onPurchase: () -> Unit,
    recommended: Boolean = false
) {
    val canAfford = currentCredits >= cost

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (recommended) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (recommended)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$hours Hour${if (hours > 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (recommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "BEST VALUE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$cost credits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onPurchase,
                enabled = canAfford && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (canAfford) "Get" else "Need ${cost - currentCredits}")
                }
            }
        }
    }
}

fun formatRemainingTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

    return when {
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ${minutes}min remaining"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} remaining"
        else -> "Less than 1 minute remaining"
    }
}
