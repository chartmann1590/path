package com.biblereadingpath.app.data.local.dao

import androidx.room.*
import com.biblereadingpath.app.data.local.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes WHERE chapterId = :chapterId ORDER BY completedAt DESC")
    fun getQuizzesForChapter(chapterId: String): Flow<List<QuizEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes ORDER BY completedAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("""
        SELECT 
            MAX(score) as bestScore,
            COUNT(*) as attemptCount,
            MAX(completedAt) as lastAttemptAt
        FROM quizzes 
        WHERE chapterId = :chapterId
    """)
    suspend fun getQuizStatsForChapter(chapterId: String): QuizStats?

    @Query("SELECT * FROM quizzes WHERE chapterId = :chapterId")
    suspend fun getQuizzesForChapterSync(chapterId: String): List<QuizEntity>
}

data class QuizStats(
    val bestScore: Int?,
    val attemptCount: Int,
    val lastAttemptAt: Long?
)

