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

data class WeightProfileUiState(
    val height: String = "",
    val prePregnancyWeight: String = "",
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val userMessage: String? = null
)

class WeightProfileViewModel : ViewModel() {
    private val repository = WeightRepository()
    private val userId = Firebase.auth.currentUser?.uid

    private val _uiState = MutableStateFlow(WeightProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, userMessage = "Usuário não encontrado.") }
            return
        }
        viewModelScope.launch {
            // Agora a chamada para getWeightProfile funciona.
            val profile = repository.getWeightProfile(userId)
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // E as referências para 'height' e 'prePregnancyWeight' também.
                        height = if (profile.height > 0) profile.height.toString() else "",
                        prePregnancyWeight = if (profile.prePregnancyWeight > 0) profile.prePregnancyWeight.toString() else ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onHeightChange(newHeight: String) {
        if (newHeight.all { it.isDigit() }) {
            _uiState.update { it.copy(height = newHeight) }
        }
    }

    fun onWeightChange(newWeight: String) {
        if (newWeight.matches(Regex("^\\d*\\.?\\d*\$"))) {
            _uiState.update { it.copy(prePregnancyWeight = newWeight) }
        }
    }

    fun saveProfile() {
        if (userId == null) {
            _uiState.update { it.copy(userMessage = "Usuário não autenticado.") }
            return
        }

        val heightValue = _uiState.value.height.toIntOrNull()
        val weightValue = _uiState.value.prePregnancyWeight.toDoubleOrNull()

        if (heightValue == null || heightValue <= 0) {
            _uiState.update { it.copy(userMessage = "Por favor, insira uma altura válida.") }
            return
        }

        if (weightValue == null || weightValue <= 0) {
            _uiState.update { it.copy(userMessage = "Por favor, insira um peso válido.") }
            return
        }

        viewModelScope.launch {
            try {
                val profile = WeightProfile(
                    height = heightValue,
                    prePregnancyWeight = weightValue
                )
                repository.saveWeightProfile(userId, profile)
                _uiState.update { it.copy(saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Erro ao salvar o perfil.") }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}