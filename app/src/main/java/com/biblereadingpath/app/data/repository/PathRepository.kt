package com.biblereadingpath.app.data.repository

import com.biblereadingpath.app.data.local.dao.FavoriteDao
import com.biblereadingpath.app.data.local.dao.NoteDao
import com.biblereadingpath.app.data.local.dao.ProgressDao
import com.biblereadingpath.app.data.local.dao.QuizDao
import com.biblereadingpath.app.data.local.entity.FavoriteEntity
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
    private val userPreferences: com.biblereadingpath.app.data.preferences.UserPreferences
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
        // Optimization: Start from stored current position instead of Genesis 1
        val storedBook = userPreferences.currentBook.first()
        val storedChapter = userPreferences.currentChapter.first()
        
        // Get all progress first
        val allProgress = progressDao.getAllProgress().first()
        val progressMap = allProgress.associateBy { it.chapterId }

        // Find starting point: if we have a stored position, start from there
        val startBookIndex = if (storedBook != null && storedBook in bibleBooks) {
            bibleBooks.indexOf(storedBook)
        } else {
            0 // Start from beginning
        }
        
        val startChapter = if (storedBook != null && storedChapter > 0) {
            storedChapter
        } else {
            1
        }

        // Iterate from stored position (or start)
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
        
        // If we started from a stored position and found nothing, check from beginning
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
        
        return Pair("Genesis", 1) // Default or finished
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
}
