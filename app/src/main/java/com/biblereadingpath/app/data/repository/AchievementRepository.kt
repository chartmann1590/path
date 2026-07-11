package com.biblereadingpath.app.data.repository

import com.biblereadingpath.app.data.local.dao.AchievementDao
import com.biblereadingpath.app.data.local.dao.FavoriteDao
import com.biblereadingpath.app.data.local.dao.NoteDao
import com.biblereadingpath.app.data.local.dao.ProgressDao
import com.biblereadingpath.app.data.local.dao.QuizDao
import com.biblereadingpath.app.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val target: Int
)

enum class AchievementCategory(val displayName: String) {
    READING("Reading"),
    STREAK("Streak"),
    NOTES("Notes"),
    QUIZZES("Quizzes"),
    DISCOVERY("Discovery")
}

object Achievements {
    val ALL = listOf(
        AchievementDefinition("first_chapter", "First Steps", "Read your first chapter", "📖", AchievementCategory.READING, 1),
        AchievementDefinition("chapters_10", "Bookworm", "Read 10 chapters", "📚", AchievementCategory.READING, 10),
        AchievementDefinition("chapters_50", "Scholar", "Read 50 chapters", "🎓", AchievementCategory.READING, 50),
        AchievementDefinition("chapters_100", "Master Reader", "Read 100 chapters", "🏅", AchievementCategory.READING, 100),
        AchievementDefinition("chapters_500", "Chapter Legend", "Read 500 chapters", "⭐", AchievementCategory.READING, 500),
        AchievementDefinition("first_book", "Book Complete", "Finish reading an entire book", "📕", AchievementCategory.READING, 1),
        AchievementDefinition("full_bible", "Biblical Scholar", "Read the entire Bible", "👑", AchievementCategory.READING, 1),

        AchievementDefinition("streak_3", "Spark", "Maintain a 3-day streak", "✨", AchievementCategory.STREAK, 3),
        AchievementDefinition("streak_7", "Flame", "Maintain a 7-day streak", "🔥", AchievementCategory.STREAK, 7),
        AchievementDefinition("streak_30", "Blaze", "Maintain a 30-day streak", "🌟", AchievementCategory.STREAK, 30),
        AchievementDefinition("streak_100", "Inferno", "Maintain a 100-day streak", "💫", AchievementCategory.STREAK, 100),
        AchievementDefinition("streak_365", "Eternal Fire", "Maintain a 365-day streak", "🔥", AchievementCategory.STREAK, 365),

        AchievementDefinition("first_note", "First Reflection", "Write your first note", "📝", AchievementCategory.NOTES, 1),
        AchievementDefinition("notes_10", "Scribe", "Write 10 notes", "✏️", AchievementCategory.NOTES, 10),
        AchievementDefinition("notes_50", "Author", "Write 50 notes", "🖊️", AchievementCategory.NOTES, 50),

        AchievementDefinition("first_quiz", "Quiz Taker", "Complete your first quiz", "🎯", AchievementCategory.QUIZZES, 1),
        AchievementDefinition("quiz_ace", "Quiz Ace", "Get perfect scores on 5 quizzes", "🏆", AchievementCategory.QUIZZES, 5),

        AchievementDefinition("first_search", "Seeker", "Perform your first search", "🔍", AchievementCategory.DISCOVERY, 1),
        AchievementDefinition("favorites_10", "Collector", "Save 10 favorite verses", "💝", AchievementCategory.DISCOVERY, 10)
    )

    fun getById(id: String): AchievementDefinition? = ALL.find { it.id == id }
}

class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val progressDao: ProgressDao,
    private val noteDao: NoteDao,
    private val favoriteDao: FavoriteDao,
    private val quizDao: QuizDao
) {
    val allAchievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    suspend fun checkAndUnlock(
        achievementId: String,
        currentValue: Int
    ): AchievementDefinition? {
        val definition = Achievements.getById(achievementId) ?: return null
        val existing = achievementDao.getAchievement(achievementId)

        if (existing != null) return null

        if (currentValue >= definition.target) {
            achievementDao.insertAchievement(
                AchievementEntity(
                    achievementId = achievementId,
                    progress = currentValue,
                    target = definition.target
                )
            )
            return definition
        }
        return null
    }

    suspend fun checkReadingAchievements(): List<AchievementDefinition> {
        val unlocked = mutableListOf<AchievementDefinition>()
        val chaptersRead = progressDao.getAllProgress().first().count { it.isCompleted }

        val readingAchievements = mapOf(
            "first_chapter" to chaptersRead,
            "chapters_10" to chaptersRead,
            "chapters_50" to chaptersRead,
            "chapters_100" to chaptersRead,
            "chapters_500" to chaptersRead
        )

        for ((id, count) in readingAchievements) {
            checkAndUnlock(id, count)?.let { unlocked.add(it) }
        }

        val completedBooks = getCompletedBookCount()
        checkAndUnlock("first_book", completedBooks)?.let { unlocked.add(it) }

        if (chaptersRead >= 1189) {
            checkAndUnlock("full_bible", 1)?.let { unlocked.add(it) }
        }

        return unlocked
    }

    suspend fun checkStreakAchievements(streak: Int): List<AchievementDefinition> {
        val unlocked = mutableListOf<AchievementDefinition>()
        val streakAchievements = mapOf(
            "streak_3" to streak,
            "streak_7" to streak,
            "streak_30" to streak,
            "streak_100" to streak,
            "streak_365" to streak
        )

        for ((id, count) in streakAchievements) {
            checkAndUnlock(id, count)?.let { unlocked.add(it) }
        }
        return unlocked
    }

    suspend fun checkNoteAchievements(): List<AchievementDefinition> {
        val unlocked = mutableListOf<AchievementDefinition>()
        val noteCount = noteDao.getAllNotes().first().size

        val noteAchievements = mapOf(
            "first_note" to noteCount,
            "notes_10" to noteCount,
            "notes_50" to noteCount
        )

        for ((id, count) in noteAchievements) {
            checkAndUnlock(id, count)?.let { unlocked.add(it) }
        }
        return unlocked
    }

    suspend fun checkQuizAchievements(): List<AchievementDefinition> {
        val unlocked = mutableListOf<AchievementDefinition>()
        val quizzes = quizDao.getAllQuizzes().first()

        checkAndUnlock("first_quiz", quizzes.size)?.let { unlocked.add(it) }

        val perfectScores = quizzes.count { it.score == it.totalQuestions && it.totalQuestions > 0 }
        checkAndUnlock("quiz_ace", perfectScores)?.let { unlocked.add(it) }

        return unlocked
    }

    suspend fun checkDiscoveryAchievements(favoriteCount: Int, hasSearched: Boolean): List<AchievementDefinition> {
        val unlocked = mutableListOf<AchievementDefinition>()

        if (hasSearched) {
            checkAndUnlock("first_search", 1)?.let { unlocked.add(it) }
        }

        checkAndUnlock("favorites_10", favoriteCount)?.let { unlocked.add(it) }

        return unlocked
    }

    private suspend fun getCompletedBookCount(): Int {
        val allProgress = progressDao.getAllProgress().first()
        val bookChapterCounts = mapOf(
            "Genesis" to 50, "Exodus" to 40, "Leviticus" to 27, "Numbers" to 36,
            "Deuteronomy" to 34, "Joshua" to 24, "Judges" to 21, "Ruth" to 4,
            "1 Samuel" to 31, "2 Samuel" to 24, "1 Kings" to 22, "2 Kings" to 25,
            "1 Chronicles" to 29, "2 Chronicles" to 36, "Ezra" to 10, "Nehemiah" to 13,
            "Esther" to 10, "Job" to 42, "Psalms" to 150, "Proverbs" to 31,
            "Ecclesiastes" to 12, "Song of Solomon" to 8, "Isaiah" to 66, "Jeremiah" to 52,
            "Lamentations" to 5, "Ezekiel" to 48, "Daniel" to 12, "Hosea" to 14,
            "Joel" to 3, "Amos" to 9, "Obadiah" to 1, "Jonah" to 4,
            "Micah" to 7, "Nahum" to 3, "Habakkuk" to 3, "Zephaniah" to 3,
            "Haggai" to 2, "Zechariah" to 14, "Malachi" to 4,
            "Matthew" to 28, "Mark" to 16, "Luke" to 24, "John" to 21,
            "Acts" to 28, "Romans" to 16, "1 Corinthians" to 16, "2 Corinthians" to 13,
            "Galatians" to 6, "Ephesians" to 6, "Philippians" to 4, "Colossians" to 4,
            "1 Thessalonians" to 5, "2 Thessalonians" to 3, "1 Timothy" to 6, "2 Timothy" to 4,
            "Titus" to 3, "Philemon" to 1, "Hebrews" to 13, "James" to 5,
            "1 Peter" to 5, "2 Peter" to 3, "1 John" to 5, "2 John" to 1,
            "3 John" to 1, "Jude" to 1, "Revelation" to 22
        )

        val completedByBook = allProgress.filter { it.isCompleted }.groupBy { it.bookName }
        var completedBooks = 0

        for ((book, totalChapters) in bookChapterCounts) {
            val chaptersCompleted = completedByBook[book]?.size ?: 0
            if (chaptersCompleted >= totalChapters) {
                completedBooks++
            }
        }

        return completedBooks
    }
}
