package br.com.gestahub.ui.medicationtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MedicationUiState(
    val medications: List<Medication> = emptyList(),
    val history: MedicationHistoryMap = emptyMap(),
    val isLoading: Boolean = true
)

class MedicationViewModel : ViewModel() {
    private val repository = MedicationRepository()

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    init {
        loadMedicationsAndHistory()
    }

    private fun loadMedicationsAndHistory() {
        viewModelScope.launch {
            val medsFlow = repository.getMedications().catch { _uiState.value = _uiState.value.copy(isLoading = false) }
            val historyFlow = repository.getHistory().catch { _uiState.value = _uiState.value.copy(isLoading = false) }

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
            // TODO: Exibir toast/snackbar de erro para o usuário
        }
    }
}