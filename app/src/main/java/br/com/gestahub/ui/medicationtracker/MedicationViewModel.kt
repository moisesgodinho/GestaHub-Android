// Local: app/src/main/java/br/com/gestahub/ui/medicationtracker/MedicationViewModel.kt
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

    // --- LÓGICA DE PAGINAÇÃO CORRIGIDA AQUI ---
    val isPreviousMonthEnabled: StateFlow<Boolean> = _uiState.combine(_currentMonth) { state, month ->
        // 1. Encontra a data do primeiro medicamento já registrado.
        val firstMedDate = state.medications
            .mapNotNull { runCatching { LocalDate.parse(it.startDate, DATE_FORMATTER) }.getOrNull() }
            .minOrNull()
            ?.let { YearMonth.from(it) }

        // 2. Define o limite máximo para voltar (2 meses antes da DUM).
        val lmpLimit = estimatedLmp?.minusMonths(2)?.let { YearMonth.from(it) }

        // 3. Determina o mês mais antigo permitido para navegação.
        //    Prioriza a data do primeiro medicamento, mas respeita o limite da DUM.
        val minMonth = when {
            firstMedDate != null && lmpLimit != null -> if (firstMedDate.isBefore(lmpLimit)) firstMedDate else lmpLimit
            firstMedDate != null -> firstMedDate
            lmpLimit != null -> lmpLimit
            else -> null // Não há limite se não tiver DUM nem medicamentos
        }

        // 4. Habilita o botão apenas se o mês atual for posterior ao mês mais antigo permitido.
        minMonth?.let { month.isAfter(it) } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


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