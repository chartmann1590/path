package com.biblereadingpath.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verses")
data class BibleVerseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val book: String,
    val chapter: Int,
    val number: Int,
    val text: String,
    val translation: String = "WEB"
)
