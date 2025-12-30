package com.biblereadingpath.app.ui

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Reader : Screen("reader/{book}/{chapter}") {
        fun createRoute(book: String, chapter: Int) = "reader/$book/$chapter"
    }
    object Notes : Screen("notes")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Downloads : Screen("downloads")
    object Streak : Screen("streak")
    object Roadmap : Screen("roadmap")
    object About : Screen("about")
    object Rewards : Screen("rewards")
    object Library : Screen("library")
    object Quiz : Screen("quiz/{book}/{chapter}") {
        fun createRoute(book: String, chapter: Int) = "quiz/$book/$chapter"
    }
}
