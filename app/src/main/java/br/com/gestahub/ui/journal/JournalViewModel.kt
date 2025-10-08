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
    private val estimatedLmp: LocalDate?
) : ViewModel() {
    private val repository = JournalRepository()
    private var entriesListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    private val _allEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val allEntries = _allEntries.asStateFlow()

    // --- LÓGICA DE NAVEGAÇÃO DUPLA ---
    private val _calendarMonth = MutableStateFlow(YearMonth.now())
    val calendarMonth = _calendarMonth.asStateFlow()

    private val _historyMonth = MutableStateFlow(YearMonth.now())
    val historyMonth = _historyMonth.asStateFlow()

    private val maxMonth = YearMonth.now()
    private val minMonth = estimatedLmp?.let { YearMonth.from(it.minusMonths(2)) }

    // Controlos para o navegador do Calendário
    val isNextCalendarMonthEnabled = calendarMonth.map { it.isBefore(maxMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isPreviousCalendarMonthEnabled = calendarMonth.map { minMonth == null || it.isAfter(minMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Controlos para o navegador do Histórico
    val isNextHistoryMonthEnabled = historyMonth.map { it.isBefore(maxMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isPreviousHistoryMonthEnabled = historyMonth.map { minMonth == null || it.isAfter(minMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    // Filtra os registos para o mês do histórico
    val entriesForHistoryMonth: StateFlow<List<JournalEntry>> =
        combine(_allEntries, _historyMonth) { entries, month ->
            entries.filter {
                val entryDate = LocalDate.parse(it.date)
                YearMonth.from(entryDate) == month
            }.sortedByDescending { it.date }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // Funções para o Calendário
    fun selectNextCalendarMonth() {
        if (isNextCalendarMonthEnabled.value) _calendarMonth.update { it.plusMonths(1) }
    }
    fun selectPreviousCalendarMonth() {
        if (isPreviousCalendarMonthEnabled.value) _calendarMonth.update { it.minusMonths(1) }
    }

    // Funções para o Histórico
    fun selectNextHistoryMonth() {
        if (isNextHistoryMonthEnabled.value) _historyMonth.update { it.plusMonths(1) }
    }
    fun selectPreviousHistoryMonth() {
        if (isPreviousHistoryMonthEnabled.value) _historyMonth.update { it.minusMonths(1) }
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

    @Suppress("UNCHECKED_CAST")
    class Factory(private val estimatedLmp: LocalDate?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JournalViewModel(estimatedLmp) as T
        }
    }
}