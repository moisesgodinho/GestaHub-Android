// Local: app/src/main/java/br/com/gestahub/ui/medicationtracker/MedicationFormViewModel.kt
package br.com.gestahub.ui.medicationtracker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.util.getTodayString // <-- IMPORTE AQUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor

data class MedicationFormUiState(
    val id: String? = null,
    val name: String = "",
    val dosage: String = "",
    val notes: String = "",
    val scheduleType: String = "FLEXIBLE",
    val doses: List<String> = listOf(""),
    val intervalHours: String = "",
    val durationType: String = "CONTINUOUS",
    val startDate: String = getTodayString(),
    val durationValue: String = "",
    val frequency: Int = 1,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val userMessage: String? = null
)

class MedicationFormViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val repository = MedicationRepository()
    private val medicationId: String? = savedStateHandle["medicationId"]

    private val _uiState = MutableStateFlow(MedicationFormUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (medicationId != null) {
            loadMedication(medicationId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadMedication(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // A busca por um único medicamento precisaria ser implementada no repositório.
            // Por enquanto, vamos filtrar da lista geral como uma solução alternativa.
            repository.getMedications().collect { meds ->
                val medToEdit = meds.find { it.id == id }
                if (medToEdit != null) {
                    val freq = if (medToEdit.scheduleType == "INTERVAL") {
                        medToEdit.intervalHours?.let { floor(24.0 / it).toInt().coerceAtLeast(1) } ?: 1
                    } else {
                        medToEdit.doses.size.coerceAtLeast(1)
                    }

                    _uiState.update {
                        it.copy(
                            id = medToEdit.id,
                            name = medToEdit.name,
                            dosage = medToEdit.dosage ?: "",
                            notes = medToEdit.notes ?: "",
                            scheduleType = medToEdit.scheduleType,
                            doses = if (medToEdit.scheduleType != "INTERVAL") medToEdit.doses else listOf(medToEdit.doses.firstOrNull() ?: ""),
                            intervalHours = medToEdit.intervalHours?.toString() ?: "",
                            durationType = medToEdit.durationType,
                            startDate = medToEdit.startDate.ifBlank { getTodayString() },
                            durationValue = medToEdit.durationValue?.toString() ?: "",
                            frequency = freq,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, userMessage = "Medicamento não encontrado.") }
                }
            }
        }
    }

    fun onFieldChange(
        name: String? = null,
        dosage: String? = null,
        notes: String? = null,
        scheduleType: String? = null,
        intervalHours: String? = null,
        durationType: String? = null,
        startDate: String? = null,
        durationValue: String? = null,
        frequency: Int? = null
    ) {
        _uiState.update { currentState ->
            val newScheduleType = scheduleType ?: currentState.scheduleType
            val newFrequency = frequency ?: currentState.frequency
            val newIntervalHours = intervalHours ?: currentState.intervalHours

            val updatedState = currentState.copy(
                name = name ?: currentState.name,
                dosage = dosage ?: currentState.dosage,
                notes = notes ?: currentState.notes,
                scheduleType = newScheduleType,
                intervalHours = newIntervalHours,
                durationType = durationType ?: currentState.durationType,
                startDate = startDate ?: currentState.startDate,
                durationValue = durationValue ?: currentState.durationValue,
                frequency = newFrequency
            )

            // Ajusta o número de doses se a frequência mudar (e não for por intervalo)
            if (frequency != null && newScheduleType != "INTERVAL") {
                val newDoses = List(newFrequency) { index ->
                    currentState.doses.getOrNull(index) ?: ""
                }
                return@update updatedState.copy(doses = newDoses)
            }

            // Recalcula a frequência se o intervalo mudar
            if (intervalHours != null && newScheduleType == "INTERVAL") {
                val interval = newIntervalHours.toIntOrNull()
                if (interval != null && interval in 1..24) {
                    val calculatedFreq = floor(24.0 / interval).toInt().coerceAtLeast(1)
                    return@update updatedState.copy(frequency = calculatedFreq)
                }
            }

            updatedState
        }
    }

    fun onDoseChange(index: Int, value: String) {
        _uiState.update { currentState ->
            val newDoses = currentState.doses.toMutableList()
            if (index < newDoses.size) {
                newDoses[index] = value
            } else {
                newDoses.add(value)
            }
            currentState.copy(doses = newDoses)
        }
    }

    fun saveMedication() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(userMessage = "O nome do medicamento é obrigatório.") }
            return
        }

        val finalDoses = when (state.scheduleType) {
            "FIXED_TIMES" -> {
                if (state.doses.any { it.isBlank() }) {
                    _uiState.update { it.copy(userMessage = "Preencha todos os horários.") }
                    return
                }
                state.doses
            }
            "INTERVAL" -> {
                if (state.doses.firstOrNull().isNullOrBlank() || state.intervalHours.isBlank()) {
                    _uiState.update { it.copy(userMessage = "Preencha o horário inicial e o intervalo.") }
                    return
                }
                state.doses.take(1)
            }
            else -> state.doses // FLEXIBLE
        }

        val medication = Medication(
            id = state.id ?: "",
            name = state.name,
            dosage = state.dosage.ifBlank { null },
            notes = state.notes.ifBlank { null },
            scheduleType = state.scheduleType,
            doses = finalDoses,
            intervalHours = if (state.scheduleType == "INTERVAL") state.intervalHours.toLongOrNull() else null,
            durationType = state.durationType,
            durationValue = if (state.durationType == "DAYS") state.durationValue.toLongOrNull() else null,
            startDate = state.startDate
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                if (state.id != null) {
                    repository.updateMedication(state.id, medication)
                } else {
                    repository.addMedication(medication)
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, userMessage = "Erro ao salvar: ${e.message}") }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}