package com.biblereadingpath.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblereadingpath.app.data.local.entity.BibleVerseEntity

@Dao
interface BibleDao {
    @Query("SELECT * FROM verses WHERE book = :book AND chapter = :chapter AND translation = :translation ORDER BY number ASC")
    suspend fun getChapter(book: String, chapter: Int, translation: String = "web"): List<BibleVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerseEntity>)

    @Query("SELECT COUNT(*) FROM verses WHERE translation = :translation")
    suspend fun getVerseCount(translation: String = "web"): Int

    @Query("SELECT DISTINCT book FROM verses WHERE translation = :translation ORDER BY book ASC")
    suspend fun getDownloadedBooks(translation: String = "web"): List<String>

    @Query("SELECT MAX(chapter) FROM verses WHERE book = :book AND translation = :translation")
    suspend fun getMaxChapter(book: String, translation: String = "web"): Int?

    @Query("SELECT COUNT(DISTINCT chapter) FROM verses WHERE book = :book AND translation = :translation")
    suspend fun getChapterCount(book: String, translation: String = "web"): Int
}
