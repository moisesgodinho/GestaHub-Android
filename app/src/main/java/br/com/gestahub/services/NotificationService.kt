package br.com.gestahub.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.gestahub.R

class NotificationService(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificações Gerais",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para notificações gerais do GestaHub"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTestNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Notificação de Teste")
            .setContentText("Esta é uma notificação de teste do GestaHub.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        const val CHANNEL_ID = "gestahub_channel"
    }
}