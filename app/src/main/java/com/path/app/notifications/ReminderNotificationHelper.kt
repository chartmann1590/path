package com.path.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.path.app.MainActivity
import com.path.app.R

class ReminderNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "bible_study_reminders"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bible Study Reminders"
            val descriptionText = "Reminders to continue your daily Bible study"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendStudyReminder(book: String, chapter: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_book", book)
            putExtra("navigate_to_chapter", chapter)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "Time for your daily Bible study! 📖",
            "Continue your spiritual journey today 🙏",
            "Your daily bread awaits! ✨",
            "Let's dive into God's Word today 💫",
            "Keep your streak going! 🔥",
            "Ready for today's reading? 📚",
            "God's Word is waiting for you 🌟"
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Path - Bible Study")
            .setContentText(messages.random())
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Continue with $book $chapter\n${messages.random()}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
