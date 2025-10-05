// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentModels.kt
package br.com.gestahub.ui.appointment

import com.google.firebase.firestore.DocumentId

// Interface comum para todos os tipos de compromissos
sealed interface Appointment {
    val id: String
    val title: String
    val date: String?
    val time: String?
    val done: Boolean // Renomeado de isDone para consistência
    val type: AppointmentType
}

enum class AppointmentType {
    MANUAL, ULTRASOUND
}

// Representa uma consulta ou exame adicionado manualmente pelo usuário
data class ManualAppointment(
    @DocumentId val documentId: String = "",
    // --- CORREÇÃO APLICADA AQUI ---
    override val title: String = "",
    override val date: String? = null,
    override val time: String? = null,
    override val done: Boolean = false,
    val professional: String? = null,
    val location: String? = null,
    val notes: String? = null
) : Appointment {
    override val id: String get() = documentId
    override val type: AppointmentType get() = AppointmentType.MANUAL
}

// Representa um ultrassom do cronograma padrão
data class UltrasoundAppointment(
    override val id: String,
    val name: String,
    val startWeek: Int,
    val endWeek: Int,
    // Campos que o usuário pode preencher
    val scheduledDate: String? = null,
    val scheduledTime: String? = null,
    val professional: String? = null,
    val location: String? = null,
    val notes: String? = null,
    // --- CORREÇÃO APLICADA AQUI ---
    override val done: Boolean = false
) : Appointment {
    override val title: String get() = name
    override val date: String? get() = scheduledDate
    override val time: String? get() = scheduledTime
    override val type: AppointmentType get() = AppointmentType.ULTRASOUND
    val isScheduled: Boolean get() = !scheduledDate.isNullOrBlank()
}