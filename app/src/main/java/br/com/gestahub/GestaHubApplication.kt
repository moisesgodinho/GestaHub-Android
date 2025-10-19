package br.com.gestahub // <-- Verifique esta linha

import android.app.Application
import br.com.gestahub.services.NotificationService // <-- Verifique o import
import br.com.gestahub.services.DailyReminderManager // Importe o novo manager

class GestaHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val notificationService = NotificationService(applicationContext)
        notificationService.createNotificationChannel()

        val dailyReminderManager = DailyReminderManager(applicationContext)
        dailyReminderManager.scheduleDailyMoodReminder()
    }
}