package br.com.gestahub.ui.medicationtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class MedicationUiState(
    val medications: List<Medication> = emptyList(),
    val history: MedicationHistoryMap = emptyMap(),
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class MedicationViewModel(private val estimatedLmp: LocalDate?) : ViewModel() {
    private val repository = MedicationRepository()

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    val isNextMonthEnabled: StateFlow<Boolean> = _uiState.combine(_currentMonth) { state, month ->
        val maxMonth = estimatedLmp?.plusDays(280)?.plusMonths(2)?.let { YearMonth.from(it) } ?: YearMonth.now().plusMonths(2)
        month.isBefore(maxMonth)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- LÓGICA DE PAGINAÇÃO FINAL E CORRETA ---
    val isPreviousMonthEnabled: StateFlow<Boolean> = _uiState.combine(_currentMonth) { state, currentMonth ->
        // Se estiver carregando ou a lista de medicamentos estiver vazia, desabilita.
        if (state.isLoading || state.medications.isEmpty()) {
            return@combine false
        }

        // 1. Encontra a data do primeiro registro de medicamento.
        val earliestMonthWithEntry = state.medications
            .asSequence()
            .mapNotNull { it.startDate.takeIf(String::isNotBlank) }
            .mapNotNull { runCatching { YearMonth.from(LocalDate.parse(it, DATE_FORMATTER)) }.getOrNull() }
            .minOrNull()

        // 2. Se por algum motivo não encontrar uma data válida, desabilita por segurança.
        if (earliestMonthWithEntry == null) {
            return@combine false
        }

        // 3. A regra final: O botão é habilitado apenas se o mês atual
        //    for estritamente posterior ao mês do primeiro registro.
        return@combine currentMonth.isAfter(earliestMonthWithEntry)

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false)


    init {
        loadMedicationsAndHistory()
    }

    fun selectNextMonth() {
        if (isNextMonthEnabled.value) {
            _currentMonth.update { it.plusMonths(1) }
        }
    }

    fun selectPreviousMonth() {
        if (isPreviousMonthEnabled.value) {
            _currentMonth.update { it.minusMonths(1) }
        }
    }

    private fun loadMedicationsAndHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val medsFlow = repository.getMedications().catch { _uiState.value = _uiState.value.copy(isLoading = false, userMessage = "Erro ao carregar medicamentos.") }
            val historyFlow = repository.getHistory().catch { _uiState.value = _uiState.value.copy(isLoading = false, userMessage = "Erro ao carregar histórico.") }

            medsFlow.combine(historyFlow) { meds, history ->
                MedicationUiState(medications = meds, history = history, isLoading = false)
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }

    fun addMedication(medication: Medication) = viewModelScope.launch {
        repository.addMedication(medication)
    }

    fun updateMedication(medId: String, medication: Medication) = viewModelScope.launch {
        repository.updateMedication(medId, medication)
    }

    fun deleteMedication(medId: String) = viewModelScope.launch {
        repository.deleteMedication(medId)
    }

    fun toggleDose(medId: String, dateString: String, doseIndex: Int) = viewModelScope.launch {
        try {
            repository.toggleDose(medId, dateString, doseIndex)
        } catch (e: Exception) {
            _uiState.update { it.copy(userMessage = e.message) }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun endMedicationFromToday(medication: Medication) {
        viewModelScope.launch {
            try {
                val startDate = LocalDate.parse(medication.startDate, DATE_FORMATTER)
                val today = LocalDate.now()

                if (startDate.isAfter(today)) {
                    deleteMedication(medication.id)
                    _uiState.update { it.copy(userMessage = "Medicamento futuro removido.") }
                    return@launch
                }

                val durationInDays = ChronoUnit.DAYS.between(startDate, today) + 1

                val updatedMedication = medication.copy(
                    durationType = "DAYS",
                    durationValue = durationInDays
                )

                repository.updateMedication(updatedMedication.id, updatedMedication)

                val tomorrow = today.plusDays(1)
                repository.clearFutureHistoryForMedication(medication.id, tomorrow.format(DATE_FORMATTER))
                _uiState.update { it.copy(userMessage = "${medication.name} foi encerrado.") }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao encerrar o medicamento.") }
            }
        }
    }
}