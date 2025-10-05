// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentsViewModel.kt
package br.com.gestahub.ui.appointment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

data class AppointmentsUiState(
    val upcomingAppointments: List<Appointment> = emptyList(),
    val pastAppointments: List<Appointment> = emptyList(),
    val lmpDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class AppointmentsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    private val _manualAppointments = MutableStateFlow<List<ManualAppointment>>(emptyList())
    private val _ultrasoundData = MutableStateFlow<Map<String, Any>>(emptyMap())
    private val _lmpDate = MutableStateFlow<LocalDate?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (userId != null) {
            listenToData(userId)
            observeAndCombineData()
        } else {
            _isLoading.value = false
            _uiState.value = AppointmentsUiState(isLoading = false, error = "Usuário não autenticado.")
        }
    }

    private fun listenToData(userId: String) {
        db.collection("users").document(userId).collection("appointments")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Erro ao buscar consultas: ${e.message}"
                    return@addSnapshotListener
                }
                _manualAppointments.value = snapshot?.documents?.mapNotNull { it.toObject<ManualAppointment>() } ?: emptyList()
            }

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Erro ao buscar perfil: ${e.message}"
                    return@addSnapshotListener
                }
                val profile = snapshot?.get("gestationalProfile") as? Map<*, *>
                val lmpString = profile?.get("lmp") as? String
                _lmpDate.value = lmpString?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

                val ultrasoundSchedule = profile?.get("ultrasoundSchedule") as? Map<String, Any> ?: emptyMap()
                _ultrasoundData.value = ultrasoundSchedule
            }
    }

    private fun observeAndCombineData() {
        viewModelScope.launch {
            combine(_manualAppointments, _ultrasoundData, _lmpDate) { manual, ultrasoundMap, lmp ->
                val ultrasoundList = AppointmentData.ultrasoundSchedule.map { baseUltrasound ->
                    val savedData = ultrasoundMap[baseUltrasound.id] as? Map<*, *>
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

                val today = LocalDate.now()
                val (past, upcoming) = allAppointments.partition {
                    it.done || (it.date?.let { d -> runCatching { LocalDate.parse(d).isBefore(today) }.getOrDefault(false) } ?: false)
                }

                AppointmentsUiState(
                    // --- CORREÇÃO APLICADA AQUI ---
                    upcomingAppointments = upcoming.sortedWith { a, b ->
                        val dateA = a.date
                        val dateB = b.date
                        when {
                            dateA == null && dateB == null -> 0
                            dateA == null -> 1
                            dateB == null -> -1
                            else -> dateA.compareTo(dateB)
                        }
                    },
                    pastAppointments = past.sortedWith { a, b ->
                        val dateA = a.date
                        val dateB = b.date
                        when {
                            dateA == null && dateB == null -> 0
                            dateA == null -> 1
                            dateB == null -> -1
                            else -> dateB.compareTo(dateA) // Ordem invertida
                        }
                    },
                    lmpDate = lmp,
                    isLoading = false
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }

    fun toggleDone(appointment: Appointment) = viewModelScope.launch {
        if (userId == null) return@launch
        val newDoneStatus = !appointment.done
        try {
            when (appointment) {
                is ManualAppointment -> {
                    val docRef = db.collection("users").document(userId)
                        .collection("appointments").document(appointment.id)
                    docRef.update("done", newDoneStatus).await()
                }
                is UltrasoundAppointment -> {
                    val docRef = db.collection("users").document(userId)
                    val fieldPath = "gestationalProfile.ultrasoundSchedule.${appointment.id}.done"
                    docRef.update(fieldPath, newDoneStatus).await()
                }
            }
            Log.d("ViewModel", "Status atualizado com sucesso!")
        } catch (e: Exception) {
            if (userId != null && appointment is UltrasoundAppointment) {
                val docRef = db.collection("users").document(userId)
                val ultrasoundUpdate = mapOf("done" to newDoneStatus)
                val scheduleUpdate = mapOf(appointment.id to ultrasoundUpdate)
                val profileUpdate = mapOf("ultrasoundSchedule" to scheduleUpdate)
                val finalUpdate = mapOf("gestationalProfile" to profileUpdate)
                docRef.set(finalUpdate, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener { Log.d("ViewModel", "Status (com merge) atualizado com sucesso!") }
                    .addOnFailureListener { fail -> _error.value = "Erro ao mesclar: ${fail.message}" }
            } else {
                _error.value = "Erro ao atualizar status: ${e.message}"
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) = viewModelScope.launch {
        if (userId == null || appointment !is ManualAppointment) return@launch
        try {
            val docRef = db.collection("users").document(userId)
                .collection("appointments").document(appointment.id)
            docRef.delete().await()
        } catch (e: Exception) {
            _error.value = "Erro ao deletar: ${e.message}"
        }
    }
}