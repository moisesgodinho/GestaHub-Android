package br.com.gestahub.ui.more.movementcounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// --- NOVO ESTADO DE UI COM CAMPO DE ERRO ---
data class MovementCounterUiState(
    val sessions: List<KickSession> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null // Adicionamos uma mensagem de erro opcional
)

class MovementCounterViewModel(
    private val repository: MovementCounterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovementCounterUiState())
    val uiState: StateFlow<MovementCounterUiState> = _uiState.asStateFlow()

    init {
        fetchKickSessions()
    }

    private fun fetchKickSessions() {
        viewModelScope.launch {
            repository.getKickSessions()
                .catch { exception -> // --- MUDANÇA IMPORTANTE AQUI ---
                    // Se o fluxo do repositório falhar, o 'catch' é acionado
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Não foi possível carregar o histórico. Verifique sua conexão ou tente mais tarde."
                    )
                }
                .collectLatest { sessions ->
                    // Se chegar aqui, os dados vieram com sucesso
                    _uiState.value = MovementCounterUiState(sessions = sessions, isLoading = false)
                }
        }
    }
    fun deleteSession(session: KickSession) {
        repository.deleteKickSession(session.id)
    }
}

class MovementCounterViewModelFactory(
    private val repository: MovementCounterRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovementCounterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovementCounterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}