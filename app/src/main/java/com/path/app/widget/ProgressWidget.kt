package com.path.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.path.app.MainActivity
import com.path.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first

class ProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPreferences = UserPreferences(context)
        val streak = userPreferences.streak.first()

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFFFFF8E1))
                        .padding(0.dp)
                        .clickable(
                            actionStartActivity(
                                Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            )
                        ),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.Top
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFA726))
                            .padding(10.dp),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        Text(
                            text = "🔥",
                            style = TextStyle(
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = "Study Streak",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.glance.unit.ColorProvider(Color.White)
                            )
                        )
                    }

                    // Streak number
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = "$streak",
                            style = TextStyle(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.glance.unit.ColorProvider(Color(0xFFFF6F00))
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = if (streak == 1) "Day" else "Days",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = androidx.glance.unit.ColorProvider(Color(0xFF5D4037))
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = when {
                                streak >= 30 -> "Amazing! 🌟"
                                streak >= 7 -> "Keep going! 💪"
                                streak >= 3 -> "Great start! ✨"
                                else -> "Build your habit!"
                            },
                            style = TextStyle(
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = androidx.glance.unit.ColorProvider(Color(0xFF6D4C41))
                            )
                        )
                    }
                }
            }
        }
    }
}
