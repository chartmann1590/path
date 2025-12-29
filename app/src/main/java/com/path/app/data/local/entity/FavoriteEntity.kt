package com.path.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val verseId: String, // e.g., "Genesis-1-1"
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val verseText: String,
    val savedAt: Long = System.currentTimeMillis()
)
