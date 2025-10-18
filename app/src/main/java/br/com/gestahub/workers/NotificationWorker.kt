package br.com.gestahub.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.gestahub.services.AppointmentReminderManager

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // A única responsabilidade do Worker agora é delegar o trabalho
            val reminderManager = AppointmentReminderManager(applicationContext)
            reminderManager.checkAndSendReminders()
            Result.success()
        } catch (e: Exception) {
            // Se algo der errado no processo, a tarefa falha
            Result.failure()
        }
    }
}