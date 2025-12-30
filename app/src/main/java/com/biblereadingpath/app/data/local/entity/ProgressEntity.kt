package com.biblereadingpath.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val chapterId: String, // e.g., "Genesis-1"
    val bookName: String,
    val chapter: Int,
    val isCompleted: Boolean,
    val completedAt: Long? = null
)
