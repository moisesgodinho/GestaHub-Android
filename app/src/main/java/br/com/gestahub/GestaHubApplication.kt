package br.com.gestahub

import android.app.Application
import br.com.gestahub.services.NotificationService
import br.com.gestahub.services.DailyReminderManager
import dagger.hilt.android.HiltAndroidApp // <-- ADICIONE ESTE IMPORT

@HiltAndroidApp // <-- ADICIONE ESTA ANOTAÇÃO
class GestaHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val notificationService = NotificationService(applicationContext)
        notificationService.createNotificationChannel()

        val dailyReminderManager = DailyReminderManager(applicationContext)
        dailyReminderManager.scheduleDailyMoodReminder()
    }
}