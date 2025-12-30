package com.biblereadingpath.app.data

/**
 * List of Bible books that are confirmed available from bible-api.com
 * Based on testing, only these books return valid content
 */
object AvailableBooks {

    /**
     * Books that are verified to work with bible-api.com
     * This list was determined through testing the API
     */
    val AVAILABLE_FROM_API = setOf(
        // Old Testament books available
        "Genesis",
        "Exodus",
        "Leviticus",
        "Numbers",
        "Deuteronomy",
        "Joshua",
        "Judges",
        "Ruth",
        "1 Samuel",
        "2 Samuel",
        "1 Kings",
        "2 Kings",
        "1 Chronicles",
        "2 Chronicles",
        "Ezra",
        "Nehemiah",
        "Esther",
        "Job",
        "Psalms",
        "Proverbs",
        "Ecclesiastes",
        "Song of Solomon",
        "Isaiah",
        "Jeremiah",
        "Lamentations",
        "Ezekiel",
        "Daniel",
        "Hosea",
        "Joel",
        "Amos",
        "Obadiah",
        "Jonah",
        "Micah",
        "Nahum",
        "Habakkuk",
        "Zephaniah",
        "Haggai",
        "Zechariah",
        "Malachi",

        // New Testament books available
        "Matthew",
        "Mark",
        "Luke",
        "John",
        "Acts",
        "Romans",
        "1 Corinthians",
        "2 Corinthians",
        "Galatians",
        "Ephesians",
        "Philippians",
        "Colossians",
        "1 Thessalonians",
        "2 Thessalonians",
        "1 Timothy",
        "2 Timothy",
        "Titus",
        "Philemon",
        "Hebrews",
        "James",
        "1 Peter",
        "2 Peter",
        "1 John",
        "2 John",
        "3 John",
        "Jude",
        "Revelation"
    )

    /**
     * Check if a book is available for download from the API
     */
    fun isAvailable(bookName: String): Boolean {
        return AVAILABLE_FROM_API.contains(bookName)
    }

    /**
     * Get list of available books from the master list
     */
    fun getAvailableBooks(): List<BibleBooks.BookInfo> {
        return BibleBooks.ALL_BOOKS.filter { isAvailable(it.name) }
    }
}
