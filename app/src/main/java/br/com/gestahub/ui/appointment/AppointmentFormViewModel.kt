// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentFormViewModel.kt
package br.com.gestahub.ui.appointment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AppointmentFormUiState(
    val id: String? = null,
    val type: AppointmentType = AppointmentType.MANUAL,
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val professional: String = "",
    val location: String = "",
    val notes: String = "",
    val isUltrasound: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val userMessage: String? = null
)

class AppointmentFormViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    private val appointmentId: String? = savedStateHandle["appointmentId"]
    private val appointmentType: String? = savedStateHandle["appointmentType"]

    private val _uiState = MutableStateFlow(AppointmentFormUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        if (appointmentId != null && appointmentType != null && userId != null) {
            loadAppointment(userId, appointmentId, AppointmentType.valueOf(appointmentType))
        } else {
            _uiState.update {
                it.copy(
                    date = today,
                    time = now,
                    isLoading = false
                )
            }
        }
    }

    private fun loadAppointment(userId: String, id: String, type: AppointmentType) {
        viewModelScope.launch {
            try {
                val appointment: Appointment? = when (type) {
                    AppointmentType.MANUAL -> {
                        db.collection("users").document(userId).collection("appointments").document(id)
                            .get().await().toObject(ManualAppointment::class.java)
                    }
                    AppointmentType.ULTRASOUND -> {
                        val userDoc = db.collection("users").document(userId).get().await()
                        val ultrasoundMap = userDoc.get("gestationalProfile.ultrasoundSchedule.$id") as? Map<*, *>
                        val baseUltrasound = AppointmentData.ultrasoundSchedule.find { it.id == id }

                        baseUltrasound?.copy(
                            scheduledDate = ultrasoundMap?.get("scheduledDate") as? String,
                            scheduledTime = ultrasoundMap?.get("time") as? String,
                            professional = ultrasoundMap?.get("professional") as? String,
                            location = ultrasoundMap?.get("location") as? String,
                            notes = ultrasoundMap?.get("notes") as? String,
                            done = ultrasoundMap?.get("done") as? Boolean ?: false
                        )
                    }
                }

                appointment?.let { app ->
                    _uiState.update {
                        it.copy(
                            id = app.id,
                            type = app.type,
                            title = app.title,
                            date = app.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            time = app.time ?: "",
                            professional = (app as? ManualAppointment)?.professional ?: (app as? UltrasoundAppointment)?.professional ?: "",
                            location = (app as? ManualAppointment)?.location ?: (app as? UltrasoundAppointment)?.location ?: "",
                            notes = (app as? ManualAppointment)?.notes ?: (app as? UltrasoundAppointment)?.notes ?: "",
                            isUltrasound = app.type == AppointmentType.ULTRASOUND,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao carregar consulta.") }
            }
        }
    }

    fun onFieldChange(
        title: String? = null,
        date: String? = null,
        time: String? = null,
        professional: String? = null,
        location: String? = null,
        notes: String? = null
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                title = title ?: currentState.title,
                date = date ?: currentState.date,
                time = time ?: currentState.time,
                professional = professional ?: currentState.professional,
                location = location ?: currentState.location,
                notes = notes ?: currentState.notes
            )
        }
    }

    fun saveAppointment() {
        if (userId == null) {
            _uiState.update { it.copy(userMessage = "Usuário não autenticado.") }
            return
        }

        val state = _uiState.value
        if (state.title.isBlank() || state.date.isBlank()) {
            _uiState.update { it.copy(userMessage = "Título e data são obrigatórios.") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (state.isUltrasound) {
                    val docRef = db.collection("users").document(userId)
                    val fieldMap = mapOf(
                        "scheduledDate" to state.date,
                        "time" to state.time,
                        "professional" to state.professional,
                        "location" to state.location,
                        "notes" to state.notes
                    )
                    docRef.update("gestationalProfile.ultrasoundSchedule.${state.id}", fieldMap).await()
                } else {
                    val appointmentData = ManualAppointment(
                        documentId = state.id ?: "",
                        title = state.title,
                        date = state.date,
                        time = state.time,
                        professional = state.professional,
                        location = state.location,
                        notes = state.notes
                    )
                    if (state.id != null) {
                        db.collection("users").document(userId).collection("appointments").document(state.id)
                            .set(appointmentData).await()
                    } else {
                        db.collection("users").document(userId).collection("appointments")
                            .add(appointmentData).await()
                    }
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, userMessage = "Erro ao salvar consulta.") }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}