package br.com.gestahub.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.gestahub.MainActivity
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

    fun showAppointmentReminderNotification(description: String, time: String, location: String) {
        val notificationId = System.currentTimeMillis().toInt()
        val title = "Lembrete de Consulta \uD83D\uDDD3\uFE0F"
        val text = "Você tem uma consulta de $description amanhã às $time em $location."

        // --- ALTERAÇÃO AQUI ---
        // Criamos uma Intent que aponta para a rota de Consultas via deep link.
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("gestahub://appointments") // URI para a tela de consultas
        ).apply {
            // Garante que a intent abra no seu app
            `package` = context.packageName
        }
        // --- FIM DA ALTERAÇÃO ---

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showDailyMoodReminderNotification() {
        val notificationId = 2 // ID único para a notificação do diário
        val title = "Como você está hoje? \uD83D\uDCDD"
        val text = "Não se esqueça de registrar seu humor e sintomas no diário de hoje!"

        // Intent para abrir a tela do diário via deep link
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("gestahub://journal")
        ).apply {
            `package` = context.packageName
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "gestahub_channel"
    }
}