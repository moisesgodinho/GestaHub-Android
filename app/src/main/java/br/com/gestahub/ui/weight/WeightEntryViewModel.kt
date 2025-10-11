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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

data class WeightEntryUiState(
    val date: Date = Date(),
    val weight: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val userMessage: String? = null
)

class WeightEntryViewModel : ViewModel() {
    private val repository = WeightRepository()
    private var userHeightCm: Int? = null

    private val _uiState = MutableStateFlow(WeightEntryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Busca a altura do usuário ao inicializar o ViewModel
        loadUserHeight()
    }

    private fun loadUserHeight() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = repository.getWeightProfile(userId)
            userHeightCm = profile?.height
        }
    }

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
        val heightInCm = userHeightCm

        if (weightValue == null || weightValue <= 0) {
            _uiState.update { it.copy(userMessage = "Por favor, insira um peso válido.") }
            return
        }

        if (heightInCm == null || heightInCm <= 0) {
            _uiState.update { it.copy(userMessage = "Não foi possível calcular o IMC. Verifique seu perfil.") }
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
                // Cálculo do IMC
                val heightInMeters = heightInCm / 100.0
                val bmi = weightValue / heightInMeters.pow(2)

                // Formata a data para "YYYY-MM-DD" para ser usada como ID
                val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(_uiState.value.date)

                val entry = WeightEntry(
                    date = dateId,
                    weight = weightValue,
                    bmi = bmi
                )

                repository.saveWeightEntry(userId, entry)
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