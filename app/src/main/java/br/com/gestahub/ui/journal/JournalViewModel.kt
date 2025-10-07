// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalViewModel.kt
package br.com.gestahub.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.JournalRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class JournalViewModel : ViewModel() {
    private val repository = JournalRepository()
    private var entriesListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadJournalEntries()
    }

    private fun loadJournalEntries() {
        _uiState.update { it.copy(isLoading = true) }
        entriesListener = repository.getJournalEntriesListener { entries ->
            _uiState.update {
                it.copy(
                    entries = entries,
                    isLoading = false
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        entriesListener?.remove()
    }
}