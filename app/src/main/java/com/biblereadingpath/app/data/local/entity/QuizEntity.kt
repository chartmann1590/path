package com.biblereadingpath.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val quizId: String, // UUID
    val chapterId: String, // e.g., "Genesis-1"
    val bookName: String,
    val chapter: Int,
    val score: Int, // Correct answers
    val totalQuestions: Int, // Always 5
    val completedAt: Long // Timestamp
)

