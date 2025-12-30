package com.biblereadingpath.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.biblereadingpath.app.analytics.FirebaseManager
import com.biblereadingpath.app.data.local.PathDatabase
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.repository.BibleRepository
import com.biblereadingpath.app.data.repository.OllamaRepository
import com.biblereadingpath.app.data.repository.PathRepository
import com.biblereadingpath.app.ui.components.AdMobBanner
import com.biblereadingpath.app.ui.components.AdMobInterstitialManager
import com.biblereadingpath.app.ui.screens.*
import androidx.compose.foundation.layout.Column

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathApp(
    initialBook: String? = null,
    initialChapter: Int? = null,
    interstitialAdManager: AdMobInterstitialManager? = null,
    rewardedAdManager: com.biblereadingpath.app.ui.components.AdMobRewardedManager? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Navigate to specific chapter if coming from notification
    LaunchedEffect(initialBook, initialChapter) {
        if (initialBook != null && initialChapter != null) {
            navController.navigate(Screen.Reader.createRoute(initialBook, initialChapter))
        }
    }
    
    // Manual DI for v1
    val db = remember {
        Room.databaseBuilder(context, PathDatabase::class.java, "path.db")
            .fallbackToDestructiveMigration() // Simple migration strategy for dev
            .build()
    }
    val userPreferences = remember { UserPreferences(context) }
    val bibleRepository = remember { BibleRepository(context, db.bibleDao(), userPreferences) }
    val pathRepository = remember { PathRepository(db.noteDao(), db.progressDao(), db.favoriteDao(), db.quizDao(), userPreferences) }
    val firebaseManager = remember { FirebaseManager(context) }

    // Check onboarding status
    val onboardingCompleted by userPreferences.onboardingCompleted.collectAsState(initial = false)
    val startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    
    // Track previous route to detect navigation changes
    val previousRouteState = remember { mutableStateOf<String?>(null) }
    var previousRoute by previousRouteState
    
    // Trigger random ad on navigation (except when navigating to Reader screen)
    LaunchedEffect(currentRoute) {
        if (currentRoute != null && currentRoute != previousRoute && 
            currentRoute != Screen.Reader.route && 
            !currentRoute.startsWith("reader/")) {
            // Small delay to ensure screen is visible before showing ad
            kotlinx.coroutines.delay(500)
            interstitialAdManager?.tryShowAd()
        }
        previousRoute = currentRoute
    }

    Scaffold(
        bottomBar = {
            // Show bottom bar with ads on all screens except specific ones
            if (currentRoute != Screen.Onboarding.route &&
                currentRoute != Screen.Reader.route &&
                currentRoute != Screen.Search.route &&
                currentRoute != Screen.Downloads.route &&
                currentRoute != Screen.Streak.route &&
                currentRoute != Screen.Roadmap.route &&
                currentRoute != Screen.About.route) {
                Column {
                    // Add padding to ensure ad is above system navigation bar
                    AdMobBanner(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    )
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Home") },
                            selected = currentRoute == Screen.Home.route,
                            onClick = { 
                                navController.navigate(Screen.Home.route)
                                // Try to show ad on navigation
                                interstitialAdManager?.tryShowAd()
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Search") },
                            selected = currentRoute == Screen.Search.route,
                            onClick = { 
                                navController.navigate(Screen.Search.route)
                                interstitialAdManager?.tryShowAd()
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Star, contentDescription = null) },
                            label = { Text("Favorites") },
                            selected = currentRoute == Screen.Favorites.route,
                            onClick = {
                                navController.navigate(Screen.Favorites.route)
                                interstitialAdManager?.tryShowAd()
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Stars, contentDescription = null) },
                            label = { Text("Rewards") },
                            selected = currentRoute == Screen.Rewards.route,
                            onClick = {
                                navController.navigate(Screen.Rewards.route)
                                interstitialAdManager?.tryShowAd()
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Settings") },
                            selected = currentRoute == Screen.Settings.route,
                            onClick = {
                                navController.navigate(Screen.Settings.route)
                                interstitialAdManager?.tryShowAd()
                            }
                        )
                    }
                }
            } else {
                // Show just the ad banner on other screens
                // Add padding to ensure ad is above system navigation bar
                AdMobBanner(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    userPreferences = userPreferences,
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = remember { HomeViewModel(userPreferences, pathRepository, bibleRepository) },
                    userPreferences = userPreferences,
                    onStartStudy = { book, chapter ->
                        navController.navigate(Screen.Reader.createRoute(book, chapter))
                    },
                    onNavigateToStreak = {
                        navController.navigate(Screen.Streak.route)
                    },
                    onNavigateToDownloads = {
                        navController.navigate(Screen.Downloads.route)
                    },
                    onNavigateToLibrary = {
                        navController.navigate(Screen.Library.route)
                    }
                )
            }
            composable(
                route = Screen.Reader.route,
                arguments = listOf(
                    navArgument("book") { type = NavType.StringType },
                    navArgument("chapter") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val book = backStackEntry.arguments?.getString("book") ?: ""
                val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1
                ReaderScreen(
                    book = book,
                    chapterNumber = chapter,
                    viewModel = remember { ReaderViewModel(bibleRepository, pathRepository, userPreferences, firebaseManager, context) },
                    userPreferences = userPreferences,
                    onBack = { navController.popBackStack() },
                    onNextChapter = { nextBook, nextChapter ->
                        navController.navigate(Screen.Reader.createRoute(nextBook, nextChapter)) {
                            popUpTo(Screen.Reader.route) { inclusive = true }
                        }
                    },
                    onSwitchBook = { switchBook, switchChapter ->
                        navController.navigate(Screen.Reader.createRoute(switchBook, switchChapter)) {
                            popUpTo(Screen.Reader.route) { inclusive = true }
                        }
                    },
                    onTakeQuiz = { quizBook, quizChapter ->
                        navController.navigate(Screen.Quiz.createRoute(quizBook, quizChapter))
                    },
                    interstitialAdManager = interstitialAdManager
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    viewModel = remember { NotesViewModel(pathRepository) },
                    userPreferences = userPreferences
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = remember { FavoritesViewModel(pathRepository, userPreferences, firebaseManager, context) },
                    onNavigateToVerse = { book, chapter ->
                        navController.navigate(Screen.Reader.createRoute(book, chapter))
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = remember { SettingsViewModel(userPreferences, firebaseManager, context) },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = remember { SearchViewModel(bibleRepository, pathRepository, firebaseManager, userPreferences) },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVerse = { book, chapter ->
                        navController.navigate(Screen.Reader.createRoute(book, chapter))
                    }
                )
            }
            composable(Screen.Downloads.route) {
                DownloadsScreen(
                    viewModel = remember { DownloadsViewModel(bibleRepository, userPreferences) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Streak.route) {
                StreakScreen(
                    viewModel = remember { StreakViewModel(userPreferences, pathRepository, context) },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRoadmap = {
                        navController.navigate(Screen.Roadmap.route)
                    },
                    onTakeQuiz = { book, chapter ->
                        navController.navigate(Screen.Quiz.createRoute(book, chapter))
                    }
                )
            }
            composable(Screen.Roadmap.route) {
                RoadmapScreen(
                    viewModel = remember { RoadmapViewModel(pathRepository, bibleRepository, userPreferences) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Rewards.route) {
                RewardsScreen(
                    viewModel = remember { RewardsViewModel(userPreferences) },
                    rewardedAdManager = rewardedAdManager
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = remember { LibraryViewModel(bibleRepository, userPreferences) },
                    onNavigateBack = { navController.popBackStack() },
                    onBookSelected = { book, chapter ->
                        navController.navigate(Screen.Reader.createRoute(book, chapter))
                    }
                )
            }
            composable(
                route = Screen.Quiz.route,
                arguments = listOf(
                    navArgument("book") { type = NavType.StringType },
                    navArgument("chapter") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val book = backStackEntry.arguments?.getString("book") ?: ""
                val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1
                val ollamaRepository = remember { OllamaRepository(userPreferences) }
                QuizScreen(
                    viewModel = remember { 
                        QuizViewModel(
                            ollamaRepository = ollamaRepository,
                            pathRepository = pathRepository,
                            bibleRepository = bibleRepository,
                            userPreferences = userPreferences,
                            book = book,
                            chapter = chapter
                        )
                    },
                    book = book,
                    chapter = chapter,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
