package br.com.gestahub.ui.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HydrationUiState(
    val history: List<WaterIntakeEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HydrationViewModel : ViewModel() {
    private val repository = HydrationRepository()

    private val _uiState = MutableStateFlow(HydrationUiState())
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        viewModelScope.launch {
            repository.getWaterHistory().collect { result ->
                result.fold(
                    onSuccess = { history ->
                        _uiState.update { it.copy(history = history, isLoading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message, isLoading = false) }
                    }
                )
            }
        }
    }
}