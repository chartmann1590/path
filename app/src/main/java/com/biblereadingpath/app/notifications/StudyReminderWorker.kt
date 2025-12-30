package com.biblereadingpath.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biblereadingpath.app.data.local.PathDatabase
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.data.repository.PathRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.Room

class StudyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get the next chapter to study
            val db = Room.databaseBuilder(
                applicationContext,
                PathDatabase::class.java,
                "path.db"
            ).build()

            val userPreferences = UserPreferences(applicationContext)
            val pathRepository = PathRepository(
                db.noteDao(),
                db.progressDao(),
                db.favoriteDao(),
                db.quizDao(),
                userPreferences
            )

            val (book, chapter) = pathRepository.getNextChapter()

            // Send notification
            val notificationHelper = ReminderNotificationHelper(applicationContext)
            notificationHelper.sendStudyReminder(book, chapter)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
