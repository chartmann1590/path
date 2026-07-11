package com.biblereadingpath.app.data.studyplan

data class PlanChapter(
    val book: String,
    val chapter: Int,
    val label: String
)

data class PlanDefinition(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val typeKey: String,
    val chapters: List<PlanChapter>,
    val reflectionPrompts: Map<String, String> = emptyMap()
) {
    val chapterCount: Int get() = chapters.size

    fun chapterId(index: Int): String = "${chapters[index].book}-${chapters[index].chapter}"

    fun reflectionPrompt(index: Int): String? =
        reflectionPrompts[chapters[index].let { "${it.book}-${it.chapter}" }]

    fun reflectionPromptForChapter(book: String, chapter: Int): String? =
        reflectionPrompts["$book-$chapter"]
}

data class PlanProgress(
    val completedCount: Int,
    val totalCount: Int,
    val isComplete: Boolean
)
