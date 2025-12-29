package com.path.app.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object ReminderScheduler {

    private const val REMINDER_WORK_NAME = "bible_study_reminder"

    fun scheduleReminders(context: Context, startHour: Int, endHour: Int) {
        // Cancel any existing reminders first
        cancelReminders(context)

        // Calculate random delay between start and end hour
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        val delayMinutes = if (currentHour < startHour) {
            // Schedule for today during the time window
            val hoursUntilStart = startHour - currentHour
            val windowHours = endHour - startHour
            val randomHourInWindow = Random.nextInt(0, windowHours)
            val randomMinute = Random.nextInt(0, 60)
            (hoursUntilStart + randomHourInWindow) * 60 + randomMinute
        } else if (currentHour >= endHour) {
            // Schedule for tomorrow during the time window
            val hoursUntilTomorrow = 24 - currentHour + startHour
            val windowHours = endHour - startHour
            val randomHourInWindow = Random.nextInt(0, windowHours)
            val randomMinute = Random.nextInt(0, 60)
            (hoursUntilTomorrow + randomHourInWindow) * 60 + randomMinute
        } else {
            // We're in the window now, schedule within the remaining time
            val remainingHours = endHour - currentHour
            val randomHour = Random.nextInt(0, maxOf(1, remainingHours))
            val randomMinute = Random.nextInt(0, 60)
            randomHour * 60 + randomMinute
        }

        val reminderRequest = OneTimeWorkRequestBuilder<StudyReminderWorker>()
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            REMINDER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun scheduleNextReminder(context: Context, startHour: Int, endHour: Int) {
        // After a notification is sent, schedule the next one for tomorrow
        val hoursUntilTomorrow = 24
        val windowHours = endHour - startHour
        val randomHourInWindow = Random.nextInt(0, windowHours)
        val randomMinute = Random.nextInt(0, 60)
        val delayMinutes = (hoursUntilTomorrow - windowHours + randomHourInWindow) * 60 + randomMinute

        val reminderRequest = OneTimeWorkRequestBuilder<StudyReminderWorker>()
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            REMINDER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }
}
