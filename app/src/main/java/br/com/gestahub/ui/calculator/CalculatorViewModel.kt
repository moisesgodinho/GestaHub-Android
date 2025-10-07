package br.com.gestahub.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class CalculatorViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun saveLmp(lmp: String) {
        viewModelScope.launch {
            if (userId == null) {
                _saveState.value = SaveState.Error("Usuário não autenticado.")
                return@launch
            }

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
                val userDocRef = db.collection("users").document(userId)
                // Modificado: Salva apenas a DUM, sem apagar o ultrassom
                val gestationalProfile = mapOf("lmp" to lmp)
                userDocRef.set(mapOf("gestationalProfile" to gestationalProfile), SetOptions.merge()).await()
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }

    fun saveUltrasound(examDate: String, weeks: String, days: String) {
        viewModelScope.launch {
            if (userId == null) {
                _saveState.value = SaveState.Error("Usuário não autenticado.")
                return@launch
            }

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
                val userDocRef = db.collection("users").document(userId)
                val ultrasoundData = mapOf(
                    "examDate" to examDate,
                    "weeksAtExam" to weeks,
                    "daysAtExam" to days
                )
                // Modificado: Salva apenas o ultrassom, sem apagar a DUM
                val gestationalProfile = mapOf("ultrasound" to ultrasoundData)
                userDocRef.set(mapOf("gestationalProfile" to gestationalProfile), SetOptions.merge()).await()
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