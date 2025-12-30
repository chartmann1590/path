package com.biblereadingpath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

data class TutorialStep(
    val targetId: String,
    val title: String,
    val description: String,
    val anchor: Alignment = Alignment.BottomCenter
)

@Composable
fun TutorialOverlay(
    steps: List<TutorialStep>,
    currentStepIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    targetCoordinates: Map<String, LayoutCoordinates>,
    modifier: Modifier = Modifier
) {
    if (currentStepIndex >= steps.size) return

    val step = steps[currentStepIndex]
    val targetCoords = targetCoordinates[step.targetId]

    if (targetCoords != null) {
        val density = LocalDensity.current
        val position = targetCoords.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
        val size = targetCoords.size
        val targetRect = Rect(
            left = position.x,
            top = position.y,
            right = position.x + size.width,
            bottom = position.y + size.height
        )
        val screenWidth = size.width.toFloat()
        val screenHeight = size.height.toFloat()

        // Dimmed background overlay
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { } // Prevent clicks through overlay
        ) {
            // Highlight target area (cutout effect)
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { targetRect.left.toDp() },
                        y = with(density) { targetRect.top.toDp() }
                    )
                    .size(
                        width = with(density) { targetRect.width.toDp() },
                        height = with(density) { targetRect.height.toDp() }
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            )

            // Tooltip card
            val tooltipOffset = when (step.anchor) {
                Alignment.TopCenter, Alignment.TopStart, Alignment.TopEnd -> {
                    Modifier.offset(
                        x = with(density) { targetRect.left.toDp() },
                        y = with(density) { (targetRect.top - 200).toDp() }
                    )
                }
                Alignment.BottomCenter, Alignment.BottomStart, Alignment.BottomEnd -> {
                    Modifier.offset(
                        x = with(density) { targetRect.left.toDp() },
                        y = with(density) { (targetRect.bottom + 16).toDp() }
                    )
                }
                Alignment.CenterStart -> {
                    Modifier.offset(
                        x = with(density) { (targetRect.left - 300).toDp() },
                        y = with(density) { targetRect.top.toDp() }
                    )
                }
                Alignment.CenterEnd -> {
                    Modifier.offset(
                        x = with(density) { (targetRect.right + 16).toDp() },
                        y = with(density) { targetRect.top.toDp() }
                    )
                }
                else -> {
                    Modifier.offset(
                        x = with(density) { targetRect.left.toDp() },
                        y = with(density) { (targetRect.bottom + 16).toDp() }
                    )
                }
            }

            Card(
                modifier = tooltipOffset
                    .widthIn(max = 320.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onSkip,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Skip tutorial",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (currentStepIndex < steps.size - 1) {
                            TextButton(onClick = onSkip) {
                                Text("Skip")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = onNext) {
                                Text("Next")
                            }
                        } else {
                            Button(onClick = onNext) {
                                Text("Got it")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialTarget(
    stepId: String,
    onPositioned: (String, LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            onPositioned(stepId, coordinates)
        }
    ) {
        content()
    }
}

