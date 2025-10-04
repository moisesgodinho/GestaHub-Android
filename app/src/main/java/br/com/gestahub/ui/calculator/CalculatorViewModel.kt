// app/src/main/java/br/com/gestahub/ui/calculator/CalculatorViewModel.kt
package br.com.gestahub.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estados para a UI saber o que fazer
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

    fun saveLmp(lmpDate: String) {
        viewModelScope.launch {
            if (userId == null) {
                _saveState.value = SaveState.Error("Usuário não autenticado.")
                return@launch
            }
            _saveState.value = SaveState.Loading
            try {
                val userDocRef = db.collection("users").document(userId)
                // Salva a DUM e apaga os dados de ultrassom para evitar conflitos
                val data = mapOf(
                    "gestationalProfile.lmp" to lmpDate,
                    "gestationalProfile.ultrasound" to null
                )
                userDocRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Erro ao salvar DUM.")
            }
        }
    }

    fun saveUltrasound(examDate: String, weeks: String, days: String) {
        viewModelScope.launch {
            if (userId == null) {
                _saveState.value = SaveState.Error("Usuário não autenticado.")
                return@launch
            }
            _saveState.value = SaveState.Loading
            try {
                val userDocRef = db.collection("users").document(userId)
                val ultrasoundData = mapOf(
                    "examDate" to examDate,
                    "weeksAtExam" to weeks,
                    "daysAtExam" to (days.ifEmpty { "0" })
                )
                // Salva os dados de ultrassom e apaga a DUM para evitar conflitos
                val data = mapOf(
                    "gestationalProfile.ultrasound" to ultrasoundData,
                    "gestationalProfile.lmp" to null
                )
                userDocRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Erro ao salvar ultrassom.")
            }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }
}