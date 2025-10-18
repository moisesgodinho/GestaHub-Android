package br.com.gestahub

import android.app.Application
import br.com.gestahub.services.NotificationService

class GestaHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Esta parte estava faltando.
        // Ela cria o canal de notificação assim que o app abre.
        val notificationService = NotificationService(applicationContext)
        notificationService.createNotificationChannel()
    }
}