package br.com.gestahub.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder // Importação necessária
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.gestahub.MainActivity // Importação necessária
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
        val title = "Lembrete de Consulta"
        val text = "Você tem uma consulta de $description amanhã às $time em $location."

        // --- CORREÇÃO APLICADA AQUI ---
        // 1. Intent para abrir a tela principal (vai para "home" por padrão)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 2. Intent do Deep Link para a tela de consultas
        val appointmentsIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("gestahub://appointments"),
            context,
            MainActivity::class.java
        )

        // 3. Usar o TaskStackBuilder para criar a pilha de volta
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntent(mainIntent) // Adiciona a "home" à pilha
            addNextIntent(appointmentsIntent) // Adiciona "consultas" no topo
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        // --- FIM DA CORREÇÃO ---

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
        val title = "Lembrete de Diário"
        val text = "Não se esqueça de registrar seu humor hoje!"

        // --- CORREÇÃO APLICADA AQUI ---
        // 1. Intent para abrir a tela principal (vai para "home" por padrão)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 2. Intent do Deep Link para a tela do diário
        val journalIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("gestahub://journal"),
            context,
            MainActivity::class.java
        )

        // 3. Usar o TaskStackBuilder para criar a pilha de volta
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntent(mainIntent) // Adiciona a "home" à pilha
            addNextIntent(journalIntent) // Adiciona "diário" no topo
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        // --- FIM DA CORREÇÃO ---

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