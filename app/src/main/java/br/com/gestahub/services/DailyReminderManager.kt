package br.com.gestahub.services

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.com.gestahub.workers.DailyMoodReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyReminderManager(private val context: Context) {

    fun scheduleDailyMoodReminder() {
        val workManager = WorkManager.getInstance(context)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyMoodReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_mood_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminderRequest
        )
    }
}