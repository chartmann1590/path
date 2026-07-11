package com.biblereadingpath.app.ui.theme

import androidx.compose.ui.graphics.Color

enum class ReaderTheme(val displayName: String, val backgroundColor: Color, val surfaceColor: Color, val textColor: Color, val verseNumberColor: Color) {
    LIGHT("Light", Color(0xFFFFFBFE), Color(0xFFFFFBFE), Color(0xFF1C1B1F), Color(0xFF6650A4)),
    SEPIA("Sepia", SepiaBackground, SepiaSurface, SepiaText, Color(0xFF8B6914)),
    DARK("Dark", DarkReaderBackground, DarkReaderSurface, DarkReaderText, Color(0xFFB39DDB)),
    AMOLED("AMOLED Black", AmoledBlack, AmoledSurface, AmoledText, Color(0xFFCE93D8));

    companion object {
        fun fromName(name: String?): ReaderTheme {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: LIGHT
        }
    }
}
