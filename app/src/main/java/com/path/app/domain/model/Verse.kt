package com.path.app.domain.model

data class Verse(
    val book: String,
    val chapter: Int,
    val number: Int,
    val text: String
)

data class Chapter(
    val book: String,
    val number: Int,
    val verses: List<Verse>
)
