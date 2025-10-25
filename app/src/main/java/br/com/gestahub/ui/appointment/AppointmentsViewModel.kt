package br.com.gestahub.ui.appointment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.domain.usecase.GetEstimatedLmpUseCase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

data class AppointmentsUiState(
    val upcomingAppointments: List<Appointment> = emptyList(),
    val pastAppointments: List<Appointment> = emptyList(),
    val lmpDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class AppointmentsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val getEstimatedLmpUseCase = GetEstimatedLmpUseCase()

    private var manualAppointmentsListener: ListenerRegistration? = null
    private var gestationalProfileListener: ListenerRegistration? = null

    private val _manualAppointments = MutableStateFlow<List<ManualAppointment>>(emptyList())
    private val _gestationalProfile = MutableStateFlow<Map<*,*>?>(null)

    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    private var isObserving = false

    fun listenToData(userId: String) {
        if (userId.isBlank()) return

        manualAppointmentsListener = db.collection("users").document(userId).collection("appointments")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AppointmentsViewModel", "Erro ao buscar consultas", e)
                    _uiState.update { it.copy(userMessage = "Ocorreu um erro ao buscar suas consultas.") }
                    return@addSnapshotListener
                }
                _manualAppointments.value = snapshot?.documents?.mapNotNull { it.toObject<ManualAppointment>() } ?: emptyList()
            }

        gestationalProfileListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.update { it.copy(userMessage = "Erro ao buscar perfil.") }
                    return@addSnapshotListener
                }
                _gestationalProfile.value = snapshot?.get("gestationalProfile") as? Map<*, *>
            }

        if (!isObserving) {
            observeAndCombineData()
            isObserving = true
        }
    }

    private fun observeAndCombineData() {
        viewModelScope.launch {
            combine(_manualAppointments, _gestationalProfile) { manual, profile ->
                val estimatedLmp = getEstimatedLmpUseCase(profile)
                val ultrasoundScheduleMap = profile?.get("ultrasoundSchedule") as? Map<String, Any> ?: emptyMap()

                val ultrasoundList = AppointmentData.ultrasoundSchedule.map { baseUltrasound ->
                    val savedData = ultrasoundScheduleMap[baseUltrasound.id] as? Map<*, *>
                    baseUltrasound.copy(
                        scheduledDate = savedData?.get("scheduledDate") as? String,
                        scheduledTime = savedData?.get("time") as? String,
                        professional = savedData?.get("professional") as? String,
                        location = savedData?.get("location") as? String,
                        notes = savedData?.get("notes") as? String,
                        done = savedData?.get("done") as? Boolean ?: false
                    )
                }

                val allAppointments = (manual + ultrasoundList).filter { it.date != null || it.type == AppointmentType.ULTRASOUND }
                val (past, upcoming) = allAppointments.partition { it.done }

                AppointmentsUiState(
                    upcomingAppointments = upcoming.sortedWith { a, b ->
                        val dateComparison = compareValues(a.date, b.date)
                        if (dateComparison != 0) dateComparison else compareValues(a.time, b.time)
                    },
                    pastAppointments = past.sortedWith { a, b ->
                        val dateComparison = compareValues(b.date, a.date)
                        if (dateComparison != 0) dateComparison else compareValues(b.time, a.time)
                    },
                    lmpDate = estimatedLmp,
                    isLoading = false
                )
            }.collect { combinedState ->
                _uiState.value = combinedState.copy(userMessage = _uiState.value.userMessage)
            }
        }
    }

    fun clearListeners() {
        manualAppointmentsListener?.remove()
        gestationalProfileListener?.remove()
        _manualAppointments.value = emptyList()
        _gestationalProfile.value = null
        _uiState.value = AppointmentsUiState()
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }

    fun toggleDone(appointment: Appointment) = viewModelScope.launch {
        val userId = Firebase.auth.currentUser?.uid ?: return@launch
        val newDoneStatus = !appointment.done

        if (newDoneStatus) {
            if (appointment is UltrasoundAppointment && !appointment.isScheduled) {
                _uiState.update { it.copy(userMessage = "Adicione uma data ao ultrassom antes de concluí-lo.") }
                return@launch
            }

            appointment.date?.let { dateString ->
                val appointmentDate = runCatching { LocalDate.parse(dateString) }.getOrNull()
                if (appointmentDate != null && appointmentDate.isAfter(LocalDate.now())) {
                    _uiState.update { it.copy(userMessage = "Não é possível concluir uma consulta futura.") }
                    return@launch
                }
            }
        }

        try {
            when (appointment) {
                is ManualAppointment -> {
                    db.collection("users").document(userId).collection("appointments").document(appointment.id)
                        .update("done", newDoneStatus).await()
                }
                is UltrasoundAppointment -> {
                    db.collection("users").document(userId)
                        .update("gestationalProfile.ultrasoundSchedule.${appointment.id}.done", newDoneStatus).await()
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(userMessage = "Erro ao atualizar o status da consulta.") }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun deleteAppointment(appointment: Appointment) = viewModelScope.launch {
        val userId = Firebase.auth.currentUser?.uid ?: return@launch
        if (appointment !is ManualAppointment) return@launch
        try {
            db.collection("users").document(userId).collection("appointments").document(appointment.id)
                .delete().await()
        } catch (e: Exception) {
            _uiState.update { it.copy(userMessage = "Erro ao deletar consulta.") }
        }
    }

    fun clearUltrasoundSchedule(appointment: Appointment) = viewModelScope.launch {
        val userId = Firebase.auth.currentUser?.uid ?: return@launch
        if (appointment !is UltrasoundAppointment) return@launch
        try {
            val docRef = db.collection("users").document(userId)
            val updates = mapOf(
                "gestationalProfile.ultrasoundSchedule.${appointment.id}.scheduledDate" to FieldValue.delete(),
                "gestationalProfile.ultrasoundSchedule.${appointment.id}.time" to FieldValue.delete(),
                "gestationalProfile.ultrasoundSchedule.${appointment.id}.professional" to FieldValue.delete(),
                "gestationalProfile.ultrasoundSchedule.${appointment.id}.location" to FieldValue.delete(),
                "gestationalProfile.ultrasoundSchedule.${appointment.id}.notes" to FieldValue.delete()
            )
            docRef.update(updates).await()
        } catch (e: Exception) {
            _uiState.update { it.copy(userMessage = "Erro ao limpar agendamento.") }
        }
    }
}