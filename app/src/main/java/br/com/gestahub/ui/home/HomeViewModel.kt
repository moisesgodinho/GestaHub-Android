package br.com.gestahub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.AppointmentRepository
import br.com.gestahub.data.GestationalProfileRepository
import br.com.gestahub.data.WeeklyInfo
import br.com.gestahub.domain.model.GestationalInfo
import br.com.gestahub.domain.usecase.CalculateGestationalInfoUseCase
import br.com.gestahub.ui.appointment.Appointment
import br.com.gestahub.ui.appointment.ManualAppointment
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val appointmentRepository = AppointmentRepository()
    private var gestationalDataListener: ListenerRegistration? = null
    private var appointmentsListener: ListenerRegistration? = null
    private val calculateGestationalInfoUseCase = CalculateGestationalInfoUseCase()

    private val _gestationalInfo = MutableStateFlow<GestationalInfo?>(null)
    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _hasGestationalData = MutableStateFlow<Boolean?>(null)

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Combina os fluxos de dados em um único estado de UI
        viewModelScope.launch {
            combine(_gestationalInfo, _upcomingAppointments, _hasGestationalData) { info, appointments, hasData ->
                when {
                    hasData == false -> UiState(dataState = GestationalDataState.NoData)
                    info != null -> {
                        val gestationalData = GestationalData(
                            lmp = info.estimatedLmp?.toString(), // Aproximação, usado para reedição
                        )
                        UiState(
                            dataState = GestationalDataState.HasData(
                                gestationalWeeks = info.gestationalWeeks,
                                gestationalDays = info.gestationalDays,
                                dueDate = info.dueDate,
                                countdownWeeks = info.countdownWeeks,
                                countdownDays = info.countdownDays,
                                gestationalData = gestationalData,
                                weeklyInfo = info.weeklyInfo,
                                estimatedLmp = info.estimatedLmp,
                                upcomingAppointments = appointments
                            )
                        )
                    }
                    else -> UiState(dataState = GestationalDataState.Loading)
                }
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun listenToAllData(userId: String) {
        listenToGestationalData(userId)
        listenToAppointments(userId)
    }

    private fun listenToGestationalData(userId: String) {
        gestationalDataListener?.remove()
        gestationalDataListener = gestationalProfileRepository.getGestationalProfileFlow(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot == null || !snapshot.exists()) {
                _hasGestationalData.value = false
                _gestationalInfo.value = null
                return@addSnapshotListener
            }

            _hasGestationalData.value = true
            val profile = snapshot.get("gestationalProfile") as? Map<*, *>
            val rawData = GestationalData(
                lmp = profile?.get("lmp") as? String,
                ultrasoundExamDate = (profile?.get("ultrasound") as? Map<*, *>)?.get("examDate") as? String,
                weeksAtExam = (profile?.get("ultrasound") as? Map<*, *>)?.get("weeksAtExam") as? String,
                daysAtExam = (profile?.get("ultrasound") as? Map<*, *>)?.get("daysAtExam") as? String
            )
            _gestationalInfo.value = calculateGestationalInfoUseCase(rawData)
        }
    }

    private fun listenToAppointments(userId: String) {
        appointmentsListener?.remove()
        appointmentsListener = appointmentRepository.getAppointmentsFlow(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot == null) return@addSnapshotListener

            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val appointments = snapshot.documents.mapNotNull { it.toObject<ManualAppointment>() }
                .filter { it.date != null && it.date >= today && !it.done }
                .sortedBy { it.date }
                .take(3)
            _upcomingAppointments.value = appointments
        }
    }

    fun clearListeners() {
        gestationalDataListener?.remove()
        appointmentsListener?.remove()
        _gestationalInfo.value = null
        _upcomingAppointments.value = emptyList()
        _hasGestationalData.value = null
        _uiState.value = UiState()
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }
}