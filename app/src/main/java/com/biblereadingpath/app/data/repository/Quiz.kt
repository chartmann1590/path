package com.biblereadingpath.app.data.repository

data class Quiz(
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>, // 4 options
    val correctIndex: Int // 0-3
)

