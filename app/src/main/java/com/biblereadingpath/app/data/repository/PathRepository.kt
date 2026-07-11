package com.biblereadingpath.app.data.repository

import com.biblereadingpath.app.data.local.dao.FavoriteDao
import com.biblereadingpath.app.data.local.dao.HighlightDao
import com.biblereadingpath.app.data.local.dao.CollectionDao
import com.biblereadingpath.app.data.local.dao.NoteDao
import com.biblereadingpath.app.data.local.dao.ProgressDao
import com.biblereadingpath.app.data.local.dao.QuizDao
import com.biblereadingpath.app.data.local.entity.CollectionEntity
import com.biblereadingpath.app.data.local.entity.CollectionMemberEntity
import com.biblereadingpath.app.data.local.entity.FavoriteEntity
import com.biblereadingpath.app.data.local.entity.HighlightEntity
import com.biblereadingpath.app.data.local.entity.NoteEntity
import com.biblereadingpath.app.data.local.entity.ProgressEntity
import com.biblereadingpath.app.data.local.entity.QuizEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PathRepository(
    private val noteDao: NoteDao,
    private val progressDao: ProgressDao,
    private val favoriteDao: FavoriteDao,
    private val quizDao: QuizDao,
    private val userPreferences: com.biblereadingpath.app.data.preferences.UserPreferences,
    private val studyPlanRepository: StudyPlanRepository? = null,
    private val highlightDao: HighlightDao? = null,
    private val collectionDao: CollectionDao? = null
) {
    fun getNotesForChapter(book: String, chapter: Int): Flow<List<NoteEntity>> =
        noteDao.getNotesForChapter(book, chapter)

    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getAllProgress(): Flow<List<ProgressEntity>> = progressDao.getAllProgress()

    suspend fun updateProgress(progress: ProgressEntity) = progressDao.updateProgress(progress)
    
    suspend fun getProgressForChapter(chapterId: String) = progressDao.getProgressForChapter(chapterId)
    
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

    suspend fun getNextChapter(): Pair<String, Int> {
        val allProgress = progressDao.getAllProgress().first()
        val progressMap = allProgress.associateBy { it.chapterId }
        val completedIds = allProgress.filter { it.isCompleted }.map { it.chapterId }.toSet()

        if (studyPlanRepository != null) {
            val plan = studyPlanRepository.getActivePlan()
            if (plan !is com.biblereadingpath.app.data.studyplan.StudyPlan.Sequential) {
                val planNext = studyPlanRepository.getNextChapterForPlan(plan, completedIds)
                if (planNext != null) return planNext
            }
        }

        val storedBook = userPreferences.currentBook.first()
        val storedChapter = userPreferences.currentChapter.first()

        val startBookIndex = if (storedBook != null && storedBook in bibleBooks) {
            bibleBooks.indexOf(storedBook)
        } else {
            0
        }

        val startChapter = if (storedBook != null && storedChapter > 0) {
            storedChapter
        } else {
            1
        }

        for (bookIndex in startBookIndex until bibleBooks.size) {
            val book = bibleBooks[bookIndex]
            val maxChapters = if (book == "Psalms") 150 else 50
            val chapterStart = if (bookIndex == startBookIndex) startChapter else 1

            for (chapter in chapterStart..maxChapters) {
                 val id = "$book-$chapter"
                 if (!progressMap.containsKey(id) || !progressMap[id]!!.isCompleted) {
                     return Pair(book, chapter)
                 }
            }
        }

        if (storedBook != null) {
            for (book in bibleBooks) {
                val maxChapters = if (book == "Psalms") 150 else 50
                for (chapter in 1..maxChapters) {
                     val id = "$book-$chapter"
                     if (!progressMap.containsKey(id) || !progressMap[id]!!.isCompleted) {
                         return Pair(book, chapter)
                     }
                }
            }
        }

        return Pair("Genesis", 1)
    }

    // Favorites
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    suspend fun addFavorite(favorite: FavoriteEntity) = favoriteDao.insertFavorite(favorite)

    suspend fun removeFavorite(verseId: String) = favoriteDao.deleteFavoriteById(verseId)

    suspend fun isFavorite(verseId: String): Boolean = favoriteDao.isFavorite(verseId)

    // Quiz methods
    fun getQuizzesForChapter(chapterId: String): Flow<List<QuizEntity>> = quizDao.getQuizzesForChapter(chapterId)

    suspend fun insertQuiz(quiz: QuizEntity) = quizDao.insertQuiz(quiz)

    fun getAllQuizzes(): Flow<List<QuizEntity>> = quizDao.getAllQuizzes()

    suspend fun getQuizStatsForChapter(chapterId: String) = quizDao.getQuizStatsForChapter(chapterId)

    // Highlights
    fun getHighlightsForChapter(book: String, chapter: Int): Flow<List<HighlightEntity>> =
        highlightDao?.getHighlightsForChapter(book, chapter) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getAllHighlights(): Flow<List<HighlightEntity>> =
        highlightDao?.getAllHighlights() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun setHighlight(verseId: String, bookName: String, chapter: Int, verseNumber: Int, color: String) {
        highlightDao?.insertHighlight(HighlightEntity(verseId, bookName, chapter, verseNumber, color))
    }

    suspend fun removeHighlight(verseId: String) {
        highlightDao?.deleteHighlight(verseId)
    }

    suspend fun getHighlight(verseId: String): HighlightEntity? = highlightDao?.getHighlight(verseId)

    // Collections
    fun getAllCollections(): Flow<List<CollectionEntity>> =
        collectionDao?.getAllCollections() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun createCollection(name: String): Long {
        return collectionDao?.insertCollection(CollectionEntity(name = name)) ?: -1L
    }

    suspend fun deleteCollection(id: Long) {
        collectionDao?.deleteCollectionById(id)
    }

    suspend fun addVerseToCollection(collectionId: Long, verseId: String) {
        collectionDao?.addVerseToCollection(CollectionMemberEntity(collectionId = collectionId, verseId = verseId))
    }

    suspend fun removeVerseFromCollection(collectionId: Long, verseId: String) {
        collectionDao?.removeVerseFromCollection(collectionId, verseId)
    }

    fun getMembersForCollection(collectionId: Long): Flow<List<CollectionMemberEntity>> =
        collectionDao?.getMembersForCollection(collectionId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getCollectionsForVerse(verseId: String): Flow<List<CollectionMemberEntity>> =
        collectionDao?.getCollectionsForVerse(verseId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
}
