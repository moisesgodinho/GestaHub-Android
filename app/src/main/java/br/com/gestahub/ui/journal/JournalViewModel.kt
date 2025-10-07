// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalViewModel.kt
package br.com.gestahub.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.JournalRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class JournalUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class JournalViewModel(
    // Recebe a data de início da gestação
    private val estimatedLmp: LocalDate?
) : ViewModel() {
    private val repository = JournalRepository()
    private var entriesListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    private val _allEntries = MutableStateFlow<List<JournalEntry>>(emptyList())

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    // --- LÓGICA DE LIMITES DE NAVEGAÇÃO ---
    private val maxMonth = YearMonth.now()
    private val minMonth = estimatedLmp?.let { YearMonth.from(it.minusMonths(2)) }

    val isNextMonthEnabled = selectedMonth.map { it.isBefore(maxMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPreviousMonthEnabled = selectedMonth.map {
        minMonth == null || it.isAfter(minMonth)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    val entriesForSelectedMonth: StateFlow<List<JournalEntry>> =
        combine(_allEntries, _selectedMonth) { entries, month ->
            entries.filter {
                val entryDate = LocalDate.parse(it.date)
                YearMonth.from(entryDate) == month
            }.sortedByDescending { it.date }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        loadJournalEntries()
    }

    private fun loadJournalEntries() {
        _uiState.update { it.copy(isLoading = true) }
        entriesListener = repository.getJournalEntriesListener { entries ->
            _allEntries.value = entries
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectNextMonth() {
        if (isNextMonthEnabled.value) {
            _selectedMonth.update { it.plusMonths(1) }
        }
    }

    fun selectPreviousMonth() {
        if (isPreviousMonthEnabled.value) {
            _selectedMonth.update { it.minusMonths(1) }
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            try {
                repository.deleteJournalEntry(entry)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Falha ao excluir o registro.") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        entriesListener?.remove()
    }

    // --- FACTORY PARA CRIAR O VIEWMODEL COM PARÂMETROS ---
    @Suppress("UNCHECKED_CAST")
    class Factory(private val estimatedLmp: LocalDate?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JournalViewModel(estimatedLmp) as T
        }
    }
}