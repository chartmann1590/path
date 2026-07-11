package com.biblereadingpath.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblereadingpath.app.data.local.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights WHERE bookName = :book AND chapter = :chapter")
    fun getHighlightsForChapter(book: String, chapter: Int): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE verseId = :verseId")
    suspend fun getHighlight(verseId: String): HighlightEntity?

    @Query("SELECT * FROM highlights ORDER BY createdAt DESC")
    fun getAllHighlights(): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE verseId = :verseId")
    suspend fun deleteHighlight(verseId: String)

    @Query("SELECT COUNT(*) FROM highlights")
    suspend fun getHighlightCount(): Int
}
