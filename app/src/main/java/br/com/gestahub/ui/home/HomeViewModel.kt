package br.com.gestahub.ui.home

import androidx.lifecycle.ViewModel
import br.com.gestahub.data.AppointmentRepository
import br.com.gestahub.data.GestationalProfileRepository
import br.com.gestahub.data.WeeklyInfo
import br.com.gestahub.domain.usecase.CalculateGestationalInfoUseCase
import br.com.gestahub.ui.appointment.Appointment
import br.com.gestahub.ui.appointment.ManualAppointment
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class GestationalDataState {
    object Loading : GestationalDataState()
    object NoData : GestationalDataState()
    data class HasData(
        val gestationalWeeks: Int,
        val gestationalDays: Int,
        val dueDate: String,
        val countdownWeeks: Int,
        val countdownDays: Int,
        val gestationalData: GestationalData,
        val weeklyInfo: WeeklyInfo?,
        val estimatedLmp: LocalDate?,
        // --- NOVO CAMPO ADICIONADO ---
        val upcomingAppointments: List<Appointment> = emptyList()
    ) : GestationalDataState()
}

data class GestationalData(
    val lmp: String? = null,
    val ultrasoundExamDate: String? = null,
    val weeksAtExam: String? = null,
    val daysAtExam: String? = null
)

data class UiState(
    val dataState: GestationalDataState = GestationalDataState.Loading
)

class HomeViewModel : ViewModel() {

    private val gestationalProfileRepository = GestationalProfileRepository()
    // --- NOVO REPOSITÓRIO ADICIONADO ---
    private val appointmentRepository = AppointmentRepository()
    private var gestationalDataListener: ListenerRegistration? = null
    // --- NOVO LISTENER ADICIONADO ---
    private var appointmentsListener: ListenerRegistration? = null
    private val calculateGestationalInfoUseCase = CalculateGestationalInfoUseCase()

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun listenToGestationalData(userId: String) {
        gestationalDataListener?.remove()
        gestationalDataListener = gestationalProfileRepository.getGestationalProfileFlow(userId).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                _uiState.update { it.copy(dataState = GestationalDataState.NoData) }
                return@addSnapshotListener
            }

            val profile = snapshot.get("gestationalProfile") as? Map<*, *>
            val lmpString = profile?.get("lmp") as? String
            val ultrasoundMap = profile?.get("ultrasound") as? Map<*, *>

            val rawData = GestationalData(
                lmp = lmpString,
                ultrasoundExamDate = ultrasoundMap?.get("examDate") as? String,
                weeksAtExam = ultrasoundMap?.get("weeksAtExam") as? String,
                daysAtExam = ultrasoundMap?.get("daysAtExam") as? String
            )

            processGestationalData(rawData)
        }
    }

    // --- NOVA FUNÇÃO ADICIONADA ---
    fun listenToAppointments(userId: String) {
        appointmentsListener?.remove()
        appointmentsListener = appointmentRepository.getAppointmentsFlow(userId).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }

            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val appointments = snapshot.documents.mapNotNull { it.toObject<ManualAppointment>() }
                .filter { it.date != null && it.date >= today && !it.done }
                .sortedBy { it.date }
                .take(3)

            val currentState = _uiState.value.dataState
            if (currentState is GestationalDataState.HasData) {
                _uiState.update {
                    it.copy(
                        dataState = currentState.copy(upcomingAppointments = appointments)
                    )
                }
            }
        }
    }

    private fun processGestationalData(data: GestationalData) {
        val gestationalInfo = calculateGestationalInfoUseCase(data)

        if (gestationalInfo == null) {
            _uiState.update { it.copy(dataState = GestationalDataState.NoData) }
            return
        }

        // Mantém a lista de compromissos se ela já existir no estado
        val currentAppointments = (_uiState.value.dataState as? GestationalDataState.HasData)?.upcomingAppointments ?: emptyList()

        _uiState.update {
            it.copy(
                dataState = GestationalDataState.HasData(
                    gestationalWeeks = gestationalInfo.gestationalWeeks,
                    gestationalDays = gestationalInfo.gestationalDays,
                    dueDate = gestationalInfo.dueDate,
                    countdownWeeks = gestationalInfo.countdownWeeks,
                    countdownDays = gestationalInfo.countdownDays,
                    gestationalData = data,
                    weeklyInfo = gestationalInfo.weeklyInfo,
                    estimatedLmp = gestationalInfo.estimatedLmp,
                    upcomingAppointments = currentAppointments // Preserva os compromissos
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        gestationalDataListener?.remove()
        // --- LIMPEZA DO NOVO LISTENER ---
        appointmentsListener?.remove()
    }
}