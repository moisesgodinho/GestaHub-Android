package br.com.gestahub.services

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppointmentReminderManager(private val context: Context) {

    private val db = Firebase.firestore
    private val notificationService = NotificationService(context)

    suspend fun checkAndSendReminders() {
        val userId = Firebase.auth.currentUser?.uid ?: return

        // Define a data de "amanhã"
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Busca no Firestore por consultas agendadas para amanhã
        val querySnapshot = db.collection("users")
            .document(userId)
            .collection("appointments")
            .whereEqualTo("date", tomorrowDate)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            // --- CORREÇÃO AQUI ---
            // Trocado "description" por "title" para buscar o campo correto.
            val title = document.getString("title") ?: "Consulta"
            val time = document.getString("time") ?: ""
            val location = document.getString("location") ?: "local não informado"

            // Agora passamos o "title" para a notificação
            notificationService.showAppointmentReminderNotification(title, time, location)
        }
    }
}