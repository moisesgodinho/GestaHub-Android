package br.com.gestahub.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.WeightRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class WeightEntryUiState(
    val date: Date = Date(),
    val weight: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val userMessage: String? = null
)

class WeightEntryViewModel : ViewModel() {
    private val repository = WeightRepository()

    private val _uiState = MutableStateFlow(WeightEntryUiState())
    val uiState = _uiState.asStateFlow()

    fun onDateChange(newDate: Date) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun onWeightChange(newWeight: String) {
        if (newWeight.matches(Regex("^\\d*\\.?\\d*\$"))) {
            _uiState.update { it.copy(weight = newWeight) }
        }
    }

    fun saveWeightEntry() {
        val weightValue = _uiState.value.weight.toDoubleOrNull()
        if (weightValue == null || weightValue <= 0) {
            _uiState.update { it.copy(userMessage = "Por favor, insira um peso válido.") }
            return
        }

        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isSaving = false, userMessage = "Erro: Usuário não autenticado.") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val entry = WeightEntry(
                    date = _uiState.value.date,
                    weight = weightValue
                )
                repository.addWeightEntry(userId, entry)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, userMessage = "Erro ao salvar o peso.") }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}