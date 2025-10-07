package br.com.gestahub.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.GestationalProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ... (a classe SaveState continua a mesma) ...
sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class CalculatorViewModel : ViewModel() {
    private val repository = GestationalProfileRepository() // Instancia o repositório

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun saveLmp(lmp: String) {
        viewModelScope.launch {
            val lmpDate = try {
                LocalDate.parse(lmp)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Formato de data inválido.")
                return@launch
            }

            val today = LocalDate.now()
            if (lmpDate.isAfter(today)) {
                _saveState.value = SaveState.Error("A data não pode estar no futuro.")
                return@launch
            }

            val weeksDifference = ChronoUnit.WEEKS.between(lmpDate, today)
            if (weeksDifference > 42) {
                _saveState.value = SaveState.Error("A gestação não pode ter mais de 42 semanas.")
                return@launch
            }

            _saveState.value = SaveState.Loading
            try {
                repository.saveLmp(lmp)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }

    fun saveUltrasound(examDate: String, weeks: String, days: String) {
        viewModelScope.launch {
            val examLocalDate = try {
                LocalDate.parse(examDate)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Formato de data inválido.")
                return@launch
            }

            val today = LocalDate.now()
            if (examLocalDate.isAfter(today)) {
                _saveState.value = SaveState.Error("A data do ultrassom não pode estar no futuro.")
                return@launch
            }

            val weeksInt = weeks.toIntOrNull()
            val daysInt = days.toIntOrNull()

            if (weeksInt == null || daysInt == null || weeksInt < 0 || daysInt < 0 || daysInt > 6) {
                _saveState.value = SaveState.Error("Semanas ou dias inválidos.")
                return@launch
            }

            val totalDaysAtExam = (weeksInt * 7) + daysInt
            val estimatedLmp = examLocalDate.minusDays(totalDaysAtExam.toLong())

            val weeksDifference = ChronoUnit.WEEKS.between(estimatedLmp, today)
            if (weeksDifference > 42) {
                _saveState.value = SaveState.Error("A gestação não pode ter mais de 42 semanas.")
                return@launch
            }

            _saveState.value = SaveState.Loading
            try {
                repository.saveUltrasound(examDate, weeks, days)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }
}