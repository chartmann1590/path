package com.path.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.path.app.data.local.entity.BibleVerseEntity

@Dao
interface BibleDao {
    @Query("SELECT * FROM verses WHERE book = :book AND chapter = :chapter AND translation = :translation ORDER BY number ASC")
    suspend fun getChapter(book: String, chapter: Int, translation: String = "WEB"): List<BibleVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerseEntity>)
    
    @Query("SELECT COUNT(*) FROM verses")
    suspend fun getVerseCount(): Int
}
