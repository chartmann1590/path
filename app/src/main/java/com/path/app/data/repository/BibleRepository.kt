package com.path.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.path.app.data.local.dao.BibleDao
import com.path.app.data.local.entity.BibleVerseEntity
import com.path.app.data.remote.BibleApiService
import com.path.app.domain.model.Chapter
import com.path.app.domain.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class RawVerse(
    val type: String,
    val chapterNumber: Int?,
    val verseNumber: Int?,
    val value: String?
)

class BibleRepository(
    private val context: Context,
    private val bibleDao: BibleDao
) {
    private val gson = Gson()
    
    private val api: BibleApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://bible-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BibleApiService::class.java)
    }

    suspend fun getChapter(book: String, chapterNumber: Int): Chapter? = withContext(Dispatchers.IO) {
        // 1. Check Local Database (Offline Cache)
        val localVerses = bibleDao.getChapter(book, chapterNumber)
        if (localVerses.isNotEmpty()) {
            return@withContext Chapter(
                book = book,
                number = chapterNumber,
                verses = localVerses.map { 
                    Verse(it.book, it.chapter, it.number, it.text) 
                }
            )
        }

        // 2. Fallback: Check Assets (Initial Seed - mostly for Genesis demo)
        if (book.equals("Genesis", ignoreCase = true)) {
             val assetChapter = getChapterFromAssets(book, chapterNumber)
             if (assetChapter != null) return@withContext assetChapter
        }

        // 3. Network Fetch (The "Free Online Resource")
        try {
            val response = api.getChapter("$book $chapterNumber")
            
            // Cache to DB
            val versesToSave = response.verses.map { apiVerse ->
                BibleVerseEntity(
                    book = book,
                    chapter = chapterNumber,
                    number = apiVerse.verse,
                    text = apiVerse.text.trim()
                )
            }
            bibleDao.insertVerses(versesToSave)

            return@withContext Chapter(
                book = book,
                number = chapterNumber,
                verses = versesToSave.map { Verse(it.book, it.chapter, it.number, it.text) }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    
    private suspend fun getChapterFromAssets(book: String, chapterNumber: Int): Chapter? {
         // Reusing logic from previous step, simplified
         try {
            val json = context.assets.open("bible.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<RawVerse>>() {}.type
            val rawVerses: List<RawVerse> = gson.fromJson(json, type)
            
            val verseItems = rawVerses.filter { 
                (it.type == "paragraph text" || it.type == "stanza text") && 
                it.chapterNumber == chapterNumber && 
                it.value != null 
            }
            
            if (verseItems.isEmpty()) return null

            val verses = verseItems.mapIndexed { index, item ->
                 Verse(book, chapterNumber, item.verseNumber ?: (index + 1), item.value!!.trim())
            }
            return Chapter(book, chapterNumber, verses)
         } catch (e: Exception) {
             return null
         }
    }
    
    suspend fun getRandomVerse(): Verse? = withContext(Dispatchers.IO) {
        // Simple implementation: Fetch random from DB if exists, else fallback to hardcoded
        val count = bibleDao.getVerseCount()
        if (count > 0) {
             // For a real random, we'd query a random row. 
             // Simplification: Return a genesis verse from assets if DB is emptyish
             getChapterFromAssets("Genesis", 1)?.verses?.random()
        } else {
             getChapterFromAssets("Genesis", 1)?.verses?.random()
        }
    }
}
