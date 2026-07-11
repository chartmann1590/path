package com.biblereadingpath.app.data.repository

import com.biblereadingpath.app.data.BibleBooks
import com.biblereadingpath.app.data.studyplan.DevotionalPlans
import com.biblereadingpath.app.data.studyplan.PlanDefinition
import com.biblereadingpath.app.data.studyplan.PlanProgress
import com.biblereadingpath.app.data.studyplan.StudyPlan
import com.biblereadingpath.app.data.studyplan.TopicPlans
import com.biblereadingpath.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StudyPlanRepository(
    val userPreferences: UserPreferences
) {
    val bibleBooks = listOf(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth",
        "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
        "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon",
        "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
        "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi",
        "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians",
        "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians",
        "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter",
        "1 John", "2 John", "3 John", "Jude", "Revelation"
    )

    suspend fun getActivePlan(): StudyPlan {
        val typeKey = userPreferences.studyPlanType.first()
        val id = userPreferences.studyPlanId.first()
        return StudyPlan.deserialize(typeKey, id)
    }

    fun getActivePlanFlow(): Flow<String> = userPreferences.studyPlanType

    suspend fun setActivePlan(plan: StudyPlan) {
        val (typeKey, id) = plan.serialize()
        userPreferences.setStudyPlan(typeKey, id)
    }

    fun getAllPlanDefinitions(): List<PlanDefinition> {
        val topicPlans = TopicPlans.ALL
        val devotionalPlans = DevotionalPlans.ALL
        val bookPlans = bibleBooks.map { bookName ->
            val info = BibleBooks.getBookInfo(bookName)
            PlanDefinition(
                id = "book_$bookName",
                name = bookName,
                description = "Read through the book of $bookName",
                icon = if (BibleBooks.Testament.OLD == info?.testament) "\uD83D\uDCD6" else "\u271F",
                typeKey = "book",
                chapters = (1..(info?.chapters ?: 50)).map { ch ->
                    com.biblereadingpath.app.data.studyplan.PlanChapter(bookName, ch, "$bookName $ch")
                }
            )
        }
        return topicPlans + devotionalPlans + bookPlans
    }

    fun getTopicPlans(): List<PlanDefinition> = TopicPlans.ALL
    fun getDevotionalPlans(): List<PlanDefinition> = DevotionalPlans.ALL

    fun getBookPlan(bookName: String): PlanDefinition? {
        val info = BibleBooks.getBookInfo(bookName) ?: return null
        return PlanDefinition(
            id = "book_$bookName",
            name = bookName,
            description = "Read through the book of $bookName",
            icon = "\uD83D\uDCD6",
            typeKey = "book",
            chapters = (1..info.chapters).map { ch ->
                com.biblereadingpath.app.data.studyplan.PlanChapter(bookName, ch, "$bookName $ch")
            }
        )
    }

    fun getPlanForPlan(plan: StudyPlan): PlanDefinition? = when (plan) {
        is StudyPlan.Sequential -> null
        is StudyPlan.BookBased -> getBookPlan(plan.bookName)
        is StudyPlan.TopicBased -> TopicPlans.getById(plan.topicId)
        is StudyPlan.Devotional -> DevotionalPlans.getById(plan.devotionalId)
    }

    fun getPlanDisplayName(plan: StudyPlan): String = when (plan) {
        is StudyPlan.Sequential -> "Sequential"
        is StudyPlan.BookBased -> plan.bookName
        is StudyPlan.TopicBased -> TopicPlans.getById(plan.topicId)?.name ?: "Topic"
        is StudyPlan.Devotional -> DevotionalPlans.getById(plan.devotionalId)?.name ?: "Devotional"
    }

    suspend fun getNextChapterForPlan(
        plan: StudyPlan,
        completedChapterIds: Set<String>
    ): Pair<String, Int>? {
        return when (plan) {
            is StudyPlan.Sequential -> null
            is StudyPlan.BookBased -> getNextForBook(plan.bookName, completedChapterIds)
            is StudyPlan.TopicBased -> getNextForTopic(plan.topicId, completedChapterIds)
            is StudyPlan.Devotional -> getNextForDevotional(plan.devotionalId, completedChapterIds)
        }
    }

    private fun getNextForBook(bookName: String, completedChapterIds: Set<String>): Pair<String, Int>? {
        val info = BibleBooks.getBookInfo(bookName) ?: return null
        for (ch in 1..info.chapters) {
            if ("$bookName-$ch" !in completedChapterIds) {
                return Pair(bookName, ch)
            }
        }
        return null
    }

    private fun getNextForTopic(topicId: String, completedChapterIds: Set<String>): Pair<String, Int>? {
        val plan = TopicPlans.getById(topicId) ?: return null
        for (chapter in plan.chapters) {
            if ("${chapter.book}-${chapter.chapter}" !in completedChapterIds) {
                return Pair(chapter.book, chapter.chapter)
            }
        }
        return null
    }

    private fun getNextForDevotional(devotionalId: String, completedChapterIds: Set<String>): Pair<String, Int>? {
        val plan = DevotionalPlans.getById(devotionalId) ?: return null
        for (chapter in plan.chapters) {
            if ("${chapter.book}-${chapter.chapter}" !in completedChapterIds) {
                return Pair(chapter.book, chapter.chapter)
            }
        }
        return null
    }

    fun getPlanProgress(plan: StudyPlan, completedChapterIds: Set<String>): PlanProgress {
        val planDef = getPlanForPlan(plan)
        if (planDef == null) {
            val total = bibleBooks.sumOf { book ->
                BibleBooks.getBookInfo(book)?.chapters ?: 0
            }
            val completed = completedChapterIds.size
            return PlanProgress(completed, total, completed >= total)
        }
        val total = planDef.chapters.size
        val completed = planDef.chapters.count { ch ->
            "${ch.book}-${ch.chapter}" in completedChapterIds
        }
        return PlanProgress(completed, total, completed >= total)
    }

    fun getDevotionalReflectionPrompt(plan: StudyPlan, book: String, chapter: Int): String? {
        if (plan !is StudyPlan.Devotional) return null
        val planDef = DevotionalPlans.getById(plan.devotionalId) ?: return null
        return planDef.reflectionPromptForChapter(book, chapter)
    }
}
