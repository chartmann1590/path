package com.biblereadingpath.app.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.OutputStream

object VerseShareCard {

    @Composable
    fun Preview(
        verseText: String,
        reference: String,
        modifier: Modifier = Modifier
    ) {
        val density = LocalDensity.current
        val context = LocalContext.current

        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            val width = size.width
            val height = size.height
            val padding = 40f

            drawRoundRect(
                color = Color(0xFF2C1810),
                cornerRadius = CornerRadius(24f, 24f),
                size = Size(width, height)
            )

            drawRoundRect(
                color = Color(0xFF3E2723),
                cornerRadius = CornerRadius(24f, 24f),
                topLeft = Offset(4f, 4f),
                size = Size(width - 8f, height - 8f)
            )

            val crossSize = 40f
            val crossX = width / 2f
            val crossY = padding + 10f
            drawLine(
                color = Color(0xFFFFD700),
                start = Offset(crossX, crossY),
                end = Offset(crossX, crossY + crossSize),
                strokeWidth = 4f
            )
            drawLine(
                color = Color(0xFFFFD700),
                start = Offset(crossX - crossSize / 2f, crossY + crossSize / 3f),
                end = Offset(crossX + crossSize / 2f, crossY + crossSize / 3f),
                strokeWidth = 4f
            )

            val paint = android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val textY = crossY + crossSize + 40f
            val maxWidth = width - padding * 2

            paint.color = android.graphics.Color.WHITE
            paint.textSize = with(density) { 16.sp.toPx() }
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC)

            val textPaint = paint
            val lines = mutableListOf<String>()
            var currentLine = ""
            for (word in verseText.split(" ")) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (textPaint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) lines.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine)

            val lineHeight = with(density) { 24.sp.toPx() }
            lines.forEachIndexed { index, line ->
                drawContext.canvas.nativeCanvas.drawText(
                    line,
                    width / 2f,
                    textY + index * lineHeight,
                    textPaint
                )
            }

            val refY = textY + lines.size * lineHeight + 20f
            paint.color = android.graphics.Color.parseColor("#FFD700")
            paint.textSize = with(density) { 14.sp.toPx() }
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            drawContext.canvas.nativeCanvas.drawText(
                reference,
                width / 2f,
                refY,
                paint
            )

            val appY = height - padding
            paint.color = android.graphics.Color.parseColor("#888888")
            paint.textSize = with(density) { 10.sp.toPx() }
            paint.typeface = android.graphics.Typeface.DEFAULT
            drawContext.canvas.nativeCanvas.drawText(
                "Path - Bible Study",
                width / 2f,
                appY,
                paint
            )
        }
    }

    fun saveAndShare(
        context: Context,
        verseText: String,
        reference: String,
        widthPx: Int = 1080,
        heightPx: Int = 1920
    ) {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        val bgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#2C1810")
        }
        val innerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#3E2723")
        }

        val rect = android.graphics.RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
        canvas.drawRoundRect(rect, 60f, 60f, bgPaint)
        val innerRect = android.graphics.RectF(8f, 8f, widthPx - 8f, heightPx - 8f)
        canvas.drawRoundRect(innerRect, 56f, 56f, innerPaint)

        val crossPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFD700")
            strokeWidth = 12f
        }
        val crossSize = 120f
        val crossX = widthPx / 2f
        val crossY = 160f
        canvas.drawLine(crossX, crossY, crossX, crossY + crossSize, crossPaint)
        canvas.drawLine(crossX - crossSize / 2, crossY + crossSize / 3, crossX + crossSize / 2, crossY + crossSize / 3, crossPaint)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 56f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC)
            isAntiAlias = true
        }

        val maxWidth = widthPx - 200f
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in verseText.split(" ")) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (textPaint.measureText(testLine) > maxWidth) {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        val lineHeight = 80f
        var textY = crossY + crossSize + 140f
        for (line in lines) {
            canvas.drawText(line, widthPx / 2f, textY, textPaint)
            textY += lineHeight
        }

        val refPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFD700")
            textSize = 48f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(reference, widthPx / 2f, textY + 60f, refPaint)

        val appPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#888888")
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("Path - Bible Study", widthPx / 2f, heightPx - 100f, appPaint)

        val filename = "path_verse_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Path")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = context.contentResolver.openOutputStream(uri)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)

                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_TEXT, "\"$verseText\" — $reference")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Verse Image"))
                }
            } else {
                @Suppress("DEPRECATION")
                val file = java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Path/$filename"
                )
                file.parentFile?.mkdirs()
                outputStream = file.outputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    putExtra(android.content.Intent.EXTRA_TEXT, "\"$verseText\" — $reference")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Verse Image"))
            }
        } catch (e: Exception) {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, "\"$verseText\" — $reference\n\nShared from Path - Bible Study")
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Verse"))
        } finally {
            outputStream?.close()
            bitmap.recycle()
        }
    }
}
