package com.biblereadingpath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biblereadingpath.app.ui.theme.HighlightAmber
import com.biblereadingpath.app.ui.theme.HighlightBlue
import com.biblereadingpath.app.ui.theme.HighlightGreen
import com.biblereadingpath.app.ui.theme.HighlightOrange
import com.biblereadingpath.app.ui.theme.HighlightPink

enum class HighlightColor(val displayName: String, val color: Color, val key: String) {
    YELLOW("Yellow", HighlightAmber, "yellow"),
    GREEN("Green", HighlightGreen, "green"),
    BLUE("Blue", HighlightBlue, "blue"),
    PINK("Pink", HighlightPink, "pink"),
    ORANGE("Orange", HighlightOrange, "orange");

    companion object {
        fun fromKey(key: String?): HighlightColor? =
            entries.find { it.key == key }
    }
}

@Composable
fun HighlightPicker(
    currentColor: String? = null,
    onColorSelected: (HighlightColor) -> Unit,
    onRemoveHighlight: (() -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HighlightColor.entries.forEach { highlight ->
            val isSelected = highlight.key == currentColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(highlight.color)
                    .then(
                        if (isSelected) Modifier.border(3.dp, Color.Black, CircleShape)
                        else Modifier.border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                    )
                    .clickable { onColorSelected(highlight) }
            )
        }
        if (currentColor != null && onRemoveHighlight != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                    .clickable { onRemoveHighlight() }
            )
        }
    }
}
