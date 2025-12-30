package com.biblereadingpath.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.biblereadingpath.app.data.local.dao.BibleDao
import com.biblereadingpath.app.data.local.entity.BibleVerseEntity
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.remote.BibleApiService
import com.biblereadingpath.app.domain.model.Chapter
import com.biblereadingpath.app.domain.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class RawVerse(
    val type: String,
    val chapterNumber: Int?,
    val verseNumber: Int?,
    val value: String?
)

class BibleRepository(
    private val context: Context,
    private val bibleDao: BibleDao,
    private val userPreferences: UserPreferences
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
        // Get current translation from preferences
        val translation = userPreferences.translation.first()
        
        // 1. Check Local Database (Offline Cache)
        val localVerses = bibleDao.getChapter(book, chapterNumber, translation)
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

        // 3. Network Fetch (The "Free Online Resource") with retry logic for rate limiting
        return@withContext fetchChapterWithRetry(book, chapterNumber, translation, maxRetries = 3)
    }

    /**
     * Fetch chapter from API and save to database, bypassing cache and assets.
     * Use this method during downloads to ensure chapters are saved to the database.
     */
    suspend fun fetchChapterForDownload(book: String, chapterNumber: Int): Chapter? = withContext(Dispatchers.IO) {
        val translation = userPreferences.translation.first()
        // Always fetch from API, don't check cache or assets
        return@withContext fetchChapterWithRetry(book, chapterNumber, translation, maxRetries = 3)
    }

    private suspend fun fetchChapterWithRetry(book: String, chapterNumber: Int, translation: String, maxRetries: Int): Chapter? {
        var lastException: Exception? = null
        var delayMs = 3000L // Start with 3 second delay for rate limit recovery

        repeat(maxRetries) { attempt ->
            try {
                android.util.Log.d("BibleRepo", "Fetching from API (attempt ${attempt + 1}/$maxRetries): $book $chapterNumber (translation: $translation)")
                val response = api.getChapter("$book $chapterNumber", translation)
                android.util.Log.d("BibleRepo", "API Response: ${response.verses.size} verses for $book $chapterNumber")

                if (response.verses.isEmpty()) {
                    android.util.Log.e("BibleRepo", "API returned 0 verses for $book $chapterNumber")
                    return null
                }

                // Cache to DB with translation
                val versesToSave = response.verses.map { apiVerse ->
                    BibleVerseEntity(
                        book = book,
                        chapter = chapterNumber,
                        number = apiVerse.verse,
                        text = apiVerse.text.trim(),
                        translation = translation
                    )
                }

                android.util.Log.d("BibleRepo", "Inserting ${versesToSave.size} verses into DB for $book $chapterNumber (translation: $translation)")
                try {
                    bibleDao.insertVerses(versesToSave)
                    android.util.Log.d("BibleRepo", "Successfully inserted ${versesToSave.size} verses into DB for $book $chapterNumber")
                } catch (e: Exception) {
                    android.util.Log.e("BibleRepo", "Failed to insert verses into DB for $book $chapterNumber: ${e.message}", e)
                    throw e // Re-throw to trigger retry logic
                }

                return Chapter(
                    book = book,
                    number = chapterNumber,
                    verses = versesToSave.map { Verse(it.book, it.chapter, it.number, it.text) }
                )
            } catch (e: Exception) {
                lastException = e
                
                // Handle HTTP errors (including rate limiting)
                when (e) {
                    is HttpException -> {
                        val statusCode = e.code()
                        val errorBody = e.response()?.errorBody()?.string() ?: ""
                        android.util.Log.e("BibleRepo", "HTTP error $statusCode for $book $chapterNumber: $errorBody")
                        
                        // Check if it's a 429 rate limit error
                        if (statusCode == 429) {
                            android.util.Log.w("BibleRepo", "Rate limited on $book $chapterNumber (attempt ${attempt + 1}/$maxRetries), waiting ${delayMs}ms")
                            
                            // Only retry if we have attempts left
                            if (attempt < maxRetries - 1) {
                                kotlinx.coroutines.delay(delayMs)
                                delayMs = (delayMs * 2).coerceAtMost(30000L) // Exponential backoff, max 30 seconds
                            } else {
                                android.util.Log.e("BibleRepo", "Max retries reached for rate limit on $book $chapterNumber - will need to retry later")
                                return null
                            }
                        } else if (statusCode in 500..599) {
                            // Server errors - retry with backoff
                            android.util.Log.w("BibleRepo", "Server error $statusCode for $book $chapterNumber (attempt ${attempt + 1}/$maxRetries), retrying...")
                            if (attempt < maxRetries - 1) {
                                kotlinx.coroutines.delay(delayMs)
                                delayMs *= 2
                            } else {
                                return null
                            }
                        } else {
                            // Client errors (4xx except 429) - don't retry
                            android.util.Log.e("BibleRepo", "Client error $statusCode for $book $chapterNumber: ${e.message}")
                            return null
                        }
                    }
                    is SocketTimeoutException, is UnknownHostException, is IOException -> {
                        // Network errors - retry with backoff
                        android.util.Log.w("BibleRepo", "Network error for $book $chapterNumber (attempt ${attempt + 1}/$maxRetries): ${e.message}")
                        if (attempt < maxRetries - 1) {
                            kotlinx.coroutines.delay(delayMs)
                            delayMs *= 2
                        } else {
                            android.util.Log.e("BibleRepo", "Max retries reached for network error on $book $chapterNumber")
                            return null
                        }
                    }
                    else -> {
                        // Other exceptions - log and don't retry
                        android.util.Log.e("BibleRepo", "Unexpected error fetching $book $chapterNumber: ${e.javaClass.simpleName} - ${e.message}", e)
                        return null
                    }
                }
            }
        }

        android.util.Log.e("BibleRepo", "Failed after $maxRetries attempts for $book $chapterNumber: ${lastException?.message}")
        return null
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
        val translation = userPreferences.translation.first()
        val count = bibleDao.getVerseCount(translation)
        if (count > 0) {
             // For a real random, we'd query a random row.
             // Simplification: Return a genesis verse from assets if DB is emptyish
             getChapterFromAssets("Genesis", 1)?.verses?.random()
        } else {
             getChapterFromAssets("Genesis", 1)?.verses?.random()
        }
    }

    suspend fun getDownloadedBooks(): List<String> = withContext(Dispatchers.IO) {
        val translation = userPreferences.translation.first()
        bibleDao.getDownloadedBooks(translation)
    }

    suspend fun getMaxChapter(book: String): Int = withContext(Dispatchers.IO) {
        val translation = userPreferences.translation.first()
        bibleDao.getMaxChapter(book, translation) ?: 0
    }

    suspend fun getDownloadedChapterCount(book: String): Int = withContext(Dispatchers.IO) {
        val translation = userPreferences.translation.first()
        bibleDao.getChapterCount(book, translation)
    }

    suspend fun isBookDownloaded(book: String): Boolean = withContext(Dispatchers.IO) {
        val translation = userPreferences.translation.first()
        val books = bibleDao.getDownloadedBooks(translation)
        books.contains(book)
    }

    /**
     * Check if a book has content without fetching from network
     */
    suspend fun hasLocalContent(book: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val translation = userPreferences.translation.first()
            val localVerses = bibleDao.getChapter(book, 1, translation)
            localVerses.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if a book is fully downloaded (all chapters present)
     * @param book The book name
     * @param expectedChapters The total number of chapters expected
     * @return true if all chapters are downloaded
     */
    suspend fun isBookFullyDownloaded(book: String, expectedChapters: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val translation = userPreferences.translation.first()
            val maxChapter = bibleDao.getMaxChapter(book, translation) ?: 0
            if (maxChapter < expectedChapters) {
                return@withContext false
            }

            // Check that we have verses for each chapter
            for (chapter in 1..expectedChapters) {
                val verses = bibleDao.getChapter(book, chapter, translation)
                if (verses.isEmpty()) {
                    return@withContext false
                }
            }
            return@withContext true
        } catch (e: Exception) {
            false
        }
    }
}
