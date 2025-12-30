package com.biblereadingpath.app.data

/**
 * Available Bible translations supported by bible-api.com
 * Each translation has an ID (for API calls) and a display name (for UI)
 */
data class BibleTranslation(
    val id: String,
    val displayName: String,
    val description: String? = null
)

object BibleTranslations {
    /**
     * List of commonly used Bible translations available from bible-api.com
     * Translation IDs are lowercase as required by the API
     */
    val AVAILABLE_TRANSLATIONS = listOf(
        BibleTranslation(
            id = "web",
            displayName = "World English Bible (WEB)",
            description = "A modern English translation"
        ),
        BibleTranslation(
            id = "kjv",
            displayName = "King James Version (KJV)",
            description = "Classic 1611 translation"
        ),
        BibleTranslation(
            id = "niv",
            displayName = "New International Version (NIV)",
            description = "Popular modern translation"
        ),
        BibleTranslation(
            id = "esv",
            displayName = "English Standard Version (ESV)",
            description = "Word-for-word translation"
        ),
        BibleTranslation(
            id = "nasb",
            displayName = "New American Standard Bible (NASB)",
            description = "Literal translation"
        ),
        BibleTranslation(
            id = "nlt",
            displayName = "New Living Translation (NLT)",
            description = "Thought-for-thought translation"
        ),
        BibleTranslation(
            id = "nkjv",
            displayName = "New King James Version (NKJV)",
            description = "Modern update of KJV"
        ),
        BibleTranslation(
            id = "csb",
            displayName = "Christian Standard Bible (CSB)",
            description = "Optimal blend of accuracy and readability"
        ),
        BibleTranslation(
            id = "msg",
            displayName = "The Message (MSG)",
            description = "Contemporary paraphrase"
        ),
        BibleTranslation(
            id = "amp",
            displayName = "Amplified Bible (AMP)",
            description = "Amplified with additional meanings"
        ),
        BibleTranslation(
            id = "rsv",
            displayName = "Revised Standard Version (RSV)",
            description = "Classic scholarly translation"
        ),
        BibleTranslation(
            id = "nrsv",
            displayName = "New Revised Standard Version (NRSV)",
            description = "Updated RSV"
        )
    )

    /**
     * Get translation by ID (case-insensitive)
     */
    fun getTranslationById(id: String): BibleTranslation? {
        return AVAILABLE_TRANSLATIONS.find { it.id.equals(id, ignoreCase = true) }
    }

    /**
     * Get default translation (WEB)
     */
    fun getDefault(): BibleTranslation {
        return AVAILABLE_TRANSLATIONS.first()
    }

    /**
     * Check if a translation ID is valid
     */
    fun isValidTranslation(id: String): Boolean {
        return AVAILABLE_TRANSLATIONS.any { it.id.equals(id, ignoreCase = true) }
    }
}

