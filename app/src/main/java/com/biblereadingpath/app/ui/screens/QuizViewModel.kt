package com.biblereadingpath.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.data.local.entity.QuizEntity
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.repository.BibleRepository
import com.biblereadingpath.app.data.repository.OllamaRepository
import com.biblereadingpath.app.data.repository.PathRepository
import com.biblereadingpath.app.data.repository.Quiz
import com.biblereadingpath.app.data.repository.QuizQuestion
import kotlinx.coroutines.launch
import java.util.UUID

data class QuizState(
    val quiz: Quiz? = null,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: MutableMap<Int, Int> = mutableMapOf(), // question index -> selected option index
    val isGenerating: Boolean = false,
    val error: String? = null,
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 5
)

class QuizViewModel(
    private val ollamaRepository: OllamaRepository,
    private val pathRepository: PathRepository,
    private val bibleRepository: BibleRepository,
    private val userPreferences: UserPreferences,
    private val book: String,
    private val chapter: Int
) : ViewModel() {

    var state by mutableStateOf(QuizState())
        private set

    init {
        generateQuiz()
    }

    private suspend fun getChapterText(): String? {
        val chapterData = bibleRepository.getChapter(book, chapter)
        return chapterData?.verses?.joinToString(" ") { it.text }
    }

    fun generateQuiz() {
        viewModelScope.launch {
            state = state.copy(isGenerating = true, error = null, isSubmitted = false)
            try {
                val chapterText = getChapterText()
                if (chapterText == null) {
                    state = state.copy(
                        isGenerating = false,
                        error = "Could not load chapter text. Please try again."
                    )
                    return@launch
                }
                val quiz = ollamaRepository.generateQuiz(book, chapter, chapterText)
                if (quiz != null) {
                    state = state.copy(
                        quiz = quiz,
                        isGenerating = false,
                        currentQuestionIndex = 0,
                        selectedAnswers = mutableMapOf()
                    )
                } else {
                    state = state.copy(
                        isGenerating = false,
                        error = "Failed to generate quiz. Please check your AI settings and try again."
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isGenerating = false,
                    error = "Error generating quiz: ${e.message}"
                )
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        val currentAnswers = state.selectedAnswers.toMutableMap()
        currentAnswers[questionIndex] = optionIndex
        state = state.copy(selectedAnswers = currentAnswers)
    }

    fun nextQuestion() {
        if (state.currentQuestionIndex < (state.quiz?.questions?.size ?: 0) - 1) {
            state = state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
        }
    }

    fun previousQuestion() {
        if (state.currentQuestionIndex > 0) {
            state = state.copy(currentQuestionIndex = state.currentQuestionIndex - 1)
        }
    }

    fun submitQuiz() {
        val quiz = state.quiz ?: return
        var score = 0
        
        // Calculate score
        quiz.questions.forEachIndexed { index, question ->
            val selectedAnswer = state.selectedAnswers[index]
            if (selectedAnswer == question.correctIndex) {
                score++
            }
        }
        
        state = state.copy(
            isSubmitted = true,
            score = score,
            totalQuestions = quiz.questions.size
        )
        
        // Save to database
        viewModelScope.launch {
            val chapterId = "$book-$chapter"
            val quizEntity = QuizEntity(
                quizId = UUID.randomUUID().toString(),
                chapterId = chapterId,
                bookName = book,
                chapter = chapter,
                score = score,
                totalQuestions = quiz.questions.size,
                completedAt = System.currentTimeMillis()
            )
            pathRepository.insertQuiz(quizEntity)
        }
    }

    fun retakeQuiz() {
        generateQuiz()
    }

    fun getCurrentQuestion(): QuizQuestion? {
        return state.quiz?.questions?.getOrNull(state.currentQuestionIndex)
    }

    fun isLastQuestion(): Boolean {
        val quiz = state.quiz ?: return false
        return state.currentQuestionIndex >= quiz.questions.size - 1
    }

    fun canSubmit(): Boolean {
        val quiz = state.quiz ?: return false
        // Check if all questions are answered
        return (0 until quiz.questions.size).all { state.selectedAnswers.containsKey(it) }
    }
}

