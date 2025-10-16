// app/src/main/java/br/com/gestahub/ui/movementcounter/MovementCounterViewModel.kt

package br.com.gestahub.ui.movementcounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class MovementCounterUiState(
    val sessions: List<KickSession> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSessionActive: Boolean = false,
    val kickCount: Int = 0,
    val elapsedTimeInSeconds: Long = 0L,
    val sessionStartTime: LocalDateTime? = null
)

class MovementCounterViewModel(
    private val repository: MovementCounterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovementCounterUiState())
    val uiState: StateFlow<MovementCounterUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        fetchKickSessions()
    }

    fun startSession() {
        _uiState.value = _uiState.value.copy(
            isSessionActive = true,
            kickCount = 0,
            elapsedTimeInSeconds = 0L,
            sessionStartTime = LocalDateTime.now()
        )
        timerJob = viewModelScope.launch {
            while (_uiState.value.isSessionActive) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    elapsedTimeInSeconds = _uiState.value.elapsedTimeInSeconds + 1
                )
            }
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        val session = KickSession(
            timestamp = Timestamp.now(),
            kicks = _uiState.value.kickCount,
            durationInSeconds = _uiState.value.elapsedTimeInSeconds
        )
        viewModelScope.launch {
            repository.addKickSession(session)
            _uiState.value = _uiState.value.copy(isSessionActive = false)
        }
    }

    fun incrementKickCount() {
        if (_uiState.value.isSessionActive) {
            _uiState.value = _uiState.value.copy(
                kickCount = _uiState.value.kickCount + 1
            )
        }
    }

    private fun fetchKickSessions() {
        viewModelScope.launch {
            repository.getKickSessions()
                .catch {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Não foi possível carregar o histórico. Verifique sua conexão ou tente mais tarde."
                    )
                }
                .collectLatest { sessions ->
                    _uiState.value = _uiState.value.copy(sessions = sessions, isLoading = false)
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