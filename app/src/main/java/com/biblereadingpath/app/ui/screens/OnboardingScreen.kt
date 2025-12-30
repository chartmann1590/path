package com.biblereadingpath.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    userPreferences: com.biblereadingpath.app.data.preferences.UserPreferences,
    onComplete: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            icon = "📖",
            title = "Welcome to Path",
            description = "A calm, mobile-first Bible study app designed to help you build a consistent daily reading habit through short, meaningful study sessions.",
            details = "Path removes friction and reduces overwhelm, making it easy to stay focused and engaged with God's Word every day."
        ),
        OnboardingPage(
            icon = "🔥",
            title = "Build Your Streak",
            description = "Track your daily progress and build consistency with gentle streak reminders. Every day you study builds your streak and helps you stay motivated.",
            details = "Tap your streak card to see detailed progress, completed books, and your reading journey."
        ),
        OnboardingPage(
            icon = "📝",
            title = "Take Notes & Highlight",
            description = "Save your thoughts and favorite verses as you read. All your notes and highlights are stored locally on your device.",
            details = "Tap any verse to add a note or reflection. Star verses to save them to your favorites for easy access later."
        ),
        OnboardingPage(
            icon = "🎯",
            title = "Daily Study Flow",
            description = "Each day, you'll see a Verse of the Day and your assigned reading. Read the chapter, optionally add notes, and mark it complete to update your streak.",
            details = "Start with the sequential reading plan, or explore other study plans in Settings. The app works completely offline - no account required!"
        ),
        OnboardingPage(
            icon = "⭐",
            title = "Earn Rewards & Go Ad-Free",
            description = "Watch rewarded ads to earn credits, then spend them to disable full-screen ads for extended periods. Build your streak and earn your way to an uninterrupted reading experience.",
            details = "Visit the Rewards tab to watch ads and purchase ad-free time. Credits can buy 1, 3, or 6 hours of ad-free reading. Banner ads will still appear, but full-screen ads will be disabled."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                userPreferences.setOnboardingCompleted(true)
                            }
                            onComplete()
                        }
                    ) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 12.dp else 8.dp)
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Page content
            val page = pages[currentPage]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = page.icon,
                    fontSize = 80.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = page.details,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    TextButton(
                        onClick = { currentPage-- }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentPage < pages.size - 1) {
                    Button(
                        onClick = { currentPage++ }
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                } else {
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                userPreferences.setOnboardingCompleted(true)
                            }
                            onComplete()
                        }
                    ) {
                        Text("Get Started")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Check, contentDescription = "Get Started")
                    }
                }
            }
        }
    }
}

private data class OnboardingPage(
    val icon: String,
    val title: String,
    val description: String,
    val details: String
)

