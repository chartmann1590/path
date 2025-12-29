package com.path.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
