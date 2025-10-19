package br.com.gestahub.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.gestahub.data.JournalRepository
import br.com.gestahub.services.NotificationService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyMoodReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val journalRepository = JournalRepository()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val hasEntry = journalRepository.getJournalEntry(today) != null

            if (!hasEntry) {
                val notificationService = NotificationService(applicationContext)
                notificationService.showDailyMoodReminderNotification()
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}