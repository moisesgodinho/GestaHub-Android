package br.com.gestahub.ui.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JournalEntryUiState(
    val entry: JournalEntry = JournalEntry(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JournalEntryViewModel @Inject constructor(
    private val repository: JournalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val date: String = savedStateHandle.get<String>("date")!!

    private val _uiState = MutableStateFlow(JournalEntryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val existingEntry = repository.getJournalEntry(date)
            if (existingEntry != null) {
                _uiState.update { it.copy(entry = existingEntry, isLoading = false) }
            } else {
                _uiState.update { it.copy(entry = JournalEntry(date = date), isLoading = false) }
            }
        }
    }

    fun onMoodChange(mood: String) {
        _uiState.update { it.copy(entry = it.entry.copy(mood = mood)) }
    }

    fun onSymptomToggle(symptom: String) {
        val currentSymptoms = _uiState.value.entry.symptoms.toMutableList()
        if (currentSymptoms.contains(symptom)) {
            currentSymptoms.remove(symptom)
        } else {
            currentSymptoms.add(symptom)
        }
        _uiState.update { it.copy(entry = it.entry.copy(symptoms = currentSymptoms)) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(entry = it.entry.copy(notes = notes)) }
    }

    fun saveEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveJournalEntry(_uiState.value.entry)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Erro ao salvar: ${e.message}") }
            }
        }
    }
}