// app/src/main/java/br/com/gestahub/ui/home/HomeViewModel.kt
package br.com.gestahub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.AppointmentRepository
import br.com.gestahub.data.GestationalProfileRepository
import br.com.gestahub.data.JournalRepository
import br.com.gestahub.data.WeeklyInfo
import br.com.gestahub.domain.model.GestationalInfo
import br.com.gestahub.domain.usecase.CalculateGestationalInfoUseCase
import br.com.gestahub.ui.appointment.Appointment
import br.com.gestahub.ui.appointment.ManualAppointment
import br.com.gestahub.ui.hydration.HydrationRepository
import br.com.gestahub.ui.hydration.WaterIntakeEntry
import br.com.gestahub.ui.journal.JournalEntry
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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
        val upcomingAppointments: List<Appointment> = emptyList(),
        val todayHydration: WaterIntakeEntry? = null,
        val todayJournalEntry: JournalEntry? = null
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gestationalProfileRepository: GestationalProfileRepository,
    private val appointmentRepository: AppointmentRepository,
    private val calculateGestationalInfoUseCase: CalculateGestationalInfoUseCase,
    private val hydrationRepository: HydrationRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    private var gestationalDataListener: ListenerRegistration? = null
    private var appointmentsListener: ListenerRegistration? = null

    private val _gestationalInfo = MutableStateFlow<GestationalInfo?>(null)
    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _hasGestationalData = MutableStateFlow<Boolean?>(null)
    private val _rawGestationalData = MutableStateFlow(GestationalData())
    private val _todayHydration = MutableStateFlow<WaterIntakeEntry?>(null)
    private val _todayJournalEntry = MutableStateFlow<JournalEntry?>(null)

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // --- CORREÇÃO APLICADA AQUI ---
            // Quando usamos mais de 5 fluxos, a sintaxe do `combine` muda.
            // Recebemos um array de valores (`values`) em vez de parâmetros individuais.
            combine(
                _gestationalInfo,
                _upcomingAppointments,
                _hasGestationalData,
                _rawGestationalData,
                _todayHydration,
                _todayJournalEntry
            ) { values ->
                // Extraímos cada valor do array pelo seu índice
                val info = values[0] as GestationalInfo?
                val appointments = values[1] as List<Appointment>
                val hasData = values[2] as Boolean?
                val rawData = values[3] as GestationalData
                val hydration = values[4] as WaterIntakeEntry?
                val journalEntry = values[5] as JournalEntry?

                when {
                    hasData == false -> UiState(dataState = GestationalDataState.NoData)
                    info != null -> {
                        UiState(
                            dataState = GestationalDataState.HasData(
                                gestationalWeeks = info.gestationalWeeks,
                                gestationalDays = info.gestationalDays,
                                dueDate = info.dueDate,
                                countdownWeeks = info.countdownWeeks,
                                countdownDays = info.countdownDays,
                                gestationalData = rawData,
                                weeklyInfo = info.weeklyInfo,
                                estimatedLmp = info.estimatedLmp,
                                upcomingAppointments = appointments,
                                todayHydration = hydration,
                                todayJournalEntry = journalEntry
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
        listenToHydration(userId)
        listenToJournal()
    }

    private fun listenToJournal() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            journalRepository.listenToJournalEntryForDate(today).collect { entry ->
                _todayJournalEntry.value = entry
            }
        }
    }

    fun addWater() {
        viewModelScope.launch {
            val currentData = _todayHydration.value ?: return@launch
            val newAmount = currentData.current + currentData.cupSize
            val newHistory = currentData.history + currentData.cupSize
            val newData = currentData.copy(current = newAmount, history = newHistory)
            hydrationRepository.updateWaterData(newData)
        }
    }

    fun undoLastWater() {
        viewModelScope.launch {
            val currentData = _todayHydration.value ?: return@launch
            if (currentData.history.isEmpty()) return@launch

            val lastAmount = currentData.history.last()
            val newAmount = (currentData.current - lastAmount).coerceAtLeast(0)
            val newHistory = currentData.history.dropLast(1)
            val newData = currentData.copy(current = newAmount, history = newHistory)
            hydrationRepository.updateWaterData(newData)
        }
    }

    private fun listenToHydration(userId: String) {
        viewModelScope.launch {
            hydrationRepository.listenToTodayWaterIntake().collect { result ->
                result.fold(
                    onSuccess = { todayEntry ->
                        if (todayEntry != null) {
                            _todayHydration.value = todayEntry
                        } else {
                            viewModelScope.launch {
                                val (goal, cupSize) = hydrationRepository.getProfileWaterSettings(userId)
                                val todayId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                _todayHydration.value = WaterIntakeEntry(
                                    id = todayId,
                                    goal = goal,
                                    cupSize = cupSize,
                                    date = todayId
                                )
                            }
                        }
                    },
                    onFailure = {
                        _todayHydration.value = null
                    }
                )
            }
        }
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
            _rawGestationalData.value = rawData
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
        _todayHydration.value = null
        _todayJournalEntry.value = null
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }
}