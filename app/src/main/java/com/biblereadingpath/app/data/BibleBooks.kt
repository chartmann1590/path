package com.biblereadingpath.app.data

/**
 * Centralized list of all 66 Bible books with chapter counts
 */
object BibleBooks {
    data class BookInfo(
        val name: String,
        val chapters: Int,
        val testament: Testament
    )

    enum class Testament {
        OLD, NEW
    }

    val ALL_BOOKS = listOf(
        // Old Testament (39 books)
        BookInfo("Genesis", 50, Testament.OLD),
        BookInfo("Exodus", 40, Testament.OLD),
        BookInfo("Leviticus", 27, Testament.OLD),
        BookInfo("Numbers", 36, Testament.OLD),
        BookInfo("Deuteronomy", 34, Testament.OLD),
        BookInfo("Joshua", 24, Testament.OLD),
        BookInfo("Judges", 21, Testament.OLD),
        BookInfo("Ruth", 4, Testament.OLD),
        BookInfo("1 Samuel", 31, Testament.OLD),
        BookInfo("2 Samuel", 24, Testament.OLD),
        BookInfo("1 Kings", 22, Testament.OLD),
        BookInfo("2 Kings", 25, Testament.OLD),
        BookInfo("1 Chronicles", 29, Testament.OLD),
        BookInfo("2 Chronicles", 36, Testament.OLD),
        BookInfo("Ezra", 10, Testament.OLD),
        BookInfo("Nehemiah", 13, Testament.OLD),
        BookInfo("Esther", 10, Testament.OLD),
        BookInfo("Job", 42, Testament.OLD),
        BookInfo("Psalms", 150, Testament.OLD),
        BookInfo("Proverbs", 31, Testament.OLD),
        BookInfo("Ecclesiastes", 12, Testament.OLD),
        BookInfo("Song of Solomon", 8, Testament.OLD),
        BookInfo("Isaiah", 66, Testament.OLD),
        BookInfo("Jeremiah", 52, Testament.OLD),
        BookInfo("Lamentations", 5, Testament.OLD),
        BookInfo("Ezekiel", 48, Testament.OLD),
        BookInfo("Daniel", 12, Testament.OLD),
        BookInfo("Hosea", 14, Testament.OLD),
        BookInfo("Joel", 3, Testament.OLD),
        BookInfo("Amos", 9, Testament.OLD),
        BookInfo("Obadiah", 1, Testament.OLD),
        BookInfo("Jonah", 4, Testament.OLD),
        BookInfo("Micah", 7, Testament.OLD),
        BookInfo("Nahum", 3, Testament.OLD),
        BookInfo("Habakkuk", 3, Testament.OLD),
        BookInfo("Zephaniah", 3, Testament.OLD),
        BookInfo("Haggai", 2, Testament.OLD),
        BookInfo("Zechariah", 14, Testament.OLD),
        BookInfo("Malachi", 4, Testament.OLD),

        // New Testament (27 books)
        BookInfo("Matthew", 28, Testament.NEW),
        BookInfo("Mark", 16, Testament.NEW),
        BookInfo("Luke", 24, Testament.NEW),
        BookInfo("John", 21, Testament.NEW),
        BookInfo("Acts", 28, Testament.NEW),
        BookInfo("Romans", 16, Testament.NEW),
        BookInfo("1 Corinthians", 16, Testament.NEW),
        BookInfo("2 Corinthians", 13, Testament.NEW),
        BookInfo("Galatians", 6, Testament.NEW),
        BookInfo("Ephesians", 6, Testament.NEW),
        BookInfo("Philippians", 4, Testament.NEW),
        BookInfo("Colossians", 4, Testament.NEW),
        BookInfo("1 Thessalonians", 5, Testament.NEW),
        BookInfo("2 Thessalonians", 3, Testament.NEW),
        BookInfo("1 Timothy", 6, Testament.NEW),
        BookInfo("2 Timothy", 4, Testament.NEW),
        BookInfo("Titus", 3, Testament.NEW),
        BookInfo("Philemon", 1, Testament.NEW),
        BookInfo("Hebrews", 13, Testament.NEW),
        BookInfo("James", 5, Testament.NEW),
        BookInfo("1 Peter", 5, Testament.NEW),
        BookInfo("2 Peter", 3, Testament.NEW),
        BookInfo("1 John", 5, Testament.NEW),
        BookInfo("2 John", 1, Testament.NEW),
        BookInfo("3 John", 1, Testament.NEW),
        BookInfo("Jude", 1, Testament.NEW),
        BookInfo("Revelation", 22, Testament.NEW)
    )

    fun getBookInfo(bookName: String): BookInfo? {
        return ALL_BOOKS.find { it.name.equals(bookName, ignoreCase = true) }
    }
}
