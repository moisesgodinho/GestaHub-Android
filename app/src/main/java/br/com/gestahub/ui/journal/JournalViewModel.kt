// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalViewModel.kt
package br.com.gestahub.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.JournalRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class JournalUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {
    private var entriesListener: ListenerRegistration? = null

    // A propriedade agora tem um setter privado para ser definida apenas internamente
    var estimatedLmp: LocalDate? = null
        private set

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    private val _allEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val allEntries = _allEntries.asStateFlow()

    private val _calendarMonth = MutableStateFlow(YearMonth.now())
    val calendarMonth = _calendarMonth.asStateFlow()

    private val _historyMonth = MutableStateFlow(YearMonth.now())
    val historyMonth = _historyMonth.asStateFlow()

    private val maxMonth: YearMonth = YearMonth.now()
    // A data mínima agora é um StateFlow que depende do estimatedLmp
    private val _minMonth = MutableStateFlow<YearMonth?>(null)

    val isNextCalendarMonthEnabled: StateFlow<Boolean> = _calendarMonth.map { it.isBefore(maxMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPreviousCalendarMonthEnabled: StateFlow<Boolean> = combine(_calendarMonth, _minMonth) { current, min ->
        min == null || current.isAfter(min)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isNextHistoryMonthEnabled: StateFlow<Boolean> = _historyMonth.map { it.isBefore(maxMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPreviousHistoryMonthEnabled: StateFlow<Boolean> = combine(_historyMonth, _minMonth) { current, min ->
        min == null || current.isAfter(min)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    val entriesForHistoryMonth: StateFlow<List<JournalEntry>> =
        combine(_allEntries, _historyMonth) { entries, month ->
            entries.filter {
                val entryDate = LocalDate.parse(it.date)
                YearMonth.from(entryDate) == month
            }.sortedByDescending { it.date }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Este método deve ser chamado da UI assim que 'estimatedLmp' estiver disponível.
     * Ele garante que o ViewModel seja inicializado com os dados dinâmicos necessários.
     */
    fun initialize(lmp: LocalDate?) {
        // Roda apenas uma vez para evitar recarregar os dados desnecessariamente
        if (this.estimatedLmp != null) return

        this.estimatedLmp = lmp
        this._minMonth.value = lmp?.let { YearMonth.from(it.minusMonths(2)) }
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
}