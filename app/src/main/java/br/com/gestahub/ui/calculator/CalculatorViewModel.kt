package br.com.gestahub.ui.calculator

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class CalculatorViewModel : ViewModel() {
    private val db = Firebase.firestore
    // --- CORREÇÃO APLICADA AQUI ---
    // Removemos a propriedade 'userId' daqui para buscá-la em tempo real.

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun saveLmp(lmp: String) {
        val currentUserId = Firebase.auth.currentUser?.uid // Pega o usuário atual
        if (currentUserId == null) {
            _saveState.value = SaveState.Error("Usuário não autenticado.")
            return
        }
        if (lmp.isBlank()) {
            _saveState.value = SaveState.Error("A data não pode estar em branco.")
            return
        }

        val data = hashMapOf(
            "lmp" to lmp,
            "ultrasound" to FieldValue.delete()
        )
        saveData(currentUserId, data)
    }

    fun saveUltrasound(examDate: String, weeks: String, days: String) {
        val currentUserId = Firebase.auth.currentUser?.uid // Pega o usuário atual
        if (currentUserId == null) {
            _saveState.value = SaveState.Error("Usuário não autenticado.")
            return
        }
        if (examDate.isBlank() || weeks.isBlank() || days.isBlank()) {
            _saveState.value = SaveState.Error("Todos os campos devem ser preenchidos.")
            return
        }

        val ultrasoundData = hashMapOf(
            "examDate" to examDate,
            "weeksAtExam" to weeks,
            "daysAtExam" to days
        )
        val data = hashMapOf("ultrasound" to ultrasoundData)
        saveData(currentUserId, data)
    }

    private fun saveData(userId: String, data: Map<String, Any>) {
        _saveState.value = SaveState.Loading

        val userDocRef = db.collection("users").document(userId)
        val gestationalProfile = hashMapOf("gestationalProfile" to data)

        userDocRef.set(gestationalProfile, SetOptions.merge())
            .addOnSuccessListener {
                _saveState.value = SaveState.Success
            }
            .addOnFailureListener { e ->
                _saveState.value = SaveState.Error(e.message ?: "Erro desconhecido ao salvar.")
            }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }
}