package br.com.gestahub.ui.contractionstimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date

class ContractionTimerViewModel : ViewModel() {

    private val repository = ContractionTimerRepository()

    private val _contractions = MutableStateFlow<List<Contraction>>(emptyList())
    val contractions: StateFlow<List<Contraction>> = _contractions.asStateFlow()

    private val _isTiming = MutableStateFlow(false)
    val isTiming: StateFlow<Boolean> = _isTiming.asStateFlow()

    private val _timer = MutableStateFlow(0L)
    val timer: StateFlow<Long> = _timer.asStateFlow()

    private var timerJob: Job? = null
    private var startTime: Date? = null

    init {
        loadContractions()
    }

    private fun loadContractions() {
        viewModelScope.launch {
            repository.getContractions()
                .catch { e ->
                    // TODO: Handle error appropriately
                }
                .collect {
                    _contractions.value = it
                }
        }
    }

    fun handleStartStop() {
        if (_isTiming.value) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isTiming.value = true
        startTime = Date()
        _timer.value = 0L
        timerJob = viewModelScope.launch {
            while (_isTiming.value) {
                delay(1000)
                _timer.value++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _isTiming.value = false

        val endTime = Date()
        val start = startTime
        if (start != null) {
            val duration = (endTime.time - start.time) / 1000

            val lastContraction = _contractions.value.firstOrNull()
            val frequency = lastContraction?.let {
                (start.time - it.startTime.toDate().time) / 1000
            } ?: 0L

            val newContraction = Contraction(
                startTime = Timestamp(start),
                duration = duration,
                frequency = frequency
            )

            viewModelScope.launch {
                repository.addContraction(newContraction)
            }
        }

        _timer.value = 0L
        startTime = null
    }


    fun deleteContraction(contractionId: String) {
        viewModelScope.launch {
            repository.deleteContraction(contractionId)
        }
    }
}