package com.path.app.data.local.dao

import androidx.room.*
import com.path.app.data.local.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: ProgressEntity)

    @Query("SELECT * FROM progress WHERE chapterId = :chapterId")
    suspend fun getProgressForChapter(chapterId: String): ProgressEntity?
}
