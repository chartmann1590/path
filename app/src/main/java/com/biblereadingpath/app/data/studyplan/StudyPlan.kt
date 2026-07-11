package com.biblereadingpath.app.data.studyplan

sealed class StudyPlan {
    abstract val typeKey: String

    data object Sequential : StudyPlan() {
        override val typeKey = "sequential"
    }

    data class BookBased(val bookName: String) : StudyPlan() {
        override val typeKey = "book"
    }

    data class TopicBased(val topicId: String) : StudyPlan() {
        override val typeKey = "topic"
    }

    data class Devotional(val devotionalId: String) : StudyPlan() {
        override val typeKey = "devotional"
    }

    fun serialize(): Pair<String, String?> = when (this) {
        is Sequential -> typeKey to null
        is BookBased -> typeKey to bookName
        is TopicBased -> typeKey to topicId
        is Devotional -> typeKey to devotionalId
    }

    companion object {
        fun deserialize(typeKey: String, id: String?): StudyPlan = when (typeKey) {
            "book" -> BookBased(id ?: "Genesis")
            "topic" -> TopicBased(id ?: "faith")
            "devotional" -> Devotional(id ?: "foundations_7")
            else -> Sequential
        }
    }
}
