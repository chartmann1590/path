package com.biblereadingpath.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val verseId: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val color: String,
    val createdAt: Long = System.currentTimeMillis()
)
