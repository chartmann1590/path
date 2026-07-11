package com.biblereadingpath.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: ConfettiShape
)

enum class ConfettiShape { CIRCLE, SQUARE, RECTANGLE }

private val confettiColors = listOf(
    Color(0xFFFF6F00),
    Color(0xFFFFA726),
    Color(0xFFFFD700),
    Color(0xFF6650A4),
    Color(0xFF4CAF50),
    Color(0xFFE91E63),
    Color(0xFF2196F3),
    Color(0xFFCE93D8)
)

@Composable
fun CelebrationOverlay(
    isVisible: Boolean,
    durationMs: Long = 3000,
    onFinished: () -> Unit = {}
) {
    if (!isVisible) return

    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var elapsed by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isVisible) {
        particles = List(80) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.3f,
                color = confettiColors.random(),
                size = Random.nextFloat() * 8f + 4f,
                velocityX = Random.nextFloat() * 0.004f - 0.002f,
                velocityY = Random.nextFloat() * 0.004f + 0.002f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 6f - 3f,
                shape = ConfettiShape.entries.random()
            )
        }
        elapsed = 0f

        val animatable = Animatable(0f)
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs.toInt(), easing = LinearEasing)
        ) {
            elapsed = value
            if (value >= 1f) {
                onFinished()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            particles.forEach { particle ->
                val currentY = particle.y + particle.velocityY * elapsed * height * 2
                val currentX = particle.x + particle.velocityX * elapsed * width * 2 +
                        sin(elapsed * 4 * PI.toFloat() + particle.x * 10) * 0.01f * width
                val currentRotation = particle.rotation + particle.rotationSpeed * elapsed * 360

                val alpha = if (elapsed > 0.7f) {
                    1f - ((elapsed - 0.7f) / 0.3f)
                } else 1f

                rotate(currentRotation, Offset(currentX, currentY)) {
                    when (particle.shape) {
                        ConfettiShape.CIRCLE -> {
                            drawCircle(
                                color = particle.color.copy(alpha = alpha),
                                radius = particle.size / 2,
                                center = Offset(currentX, currentY)
                            )
                        }
                        ConfettiShape.SQUARE -> {
                            drawRect(
                                color = particle.color.copy(alpha = alpha),
                                topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                                size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                            )
                        }
                        ConfettiShape.RECTANGLE -> {
                            drawRect(
                                color = particle.color.copy(alpha = alpha),
                                topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 4),
                                size = androidx.compose.ui.geometry.Size(particle.size, particle.size / 2)
                            )
                        }
                    }
                }
            }
        }
    }
}

private val PI = 3.14159265f
