package br.com.gestahub.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.WeightRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightProfileUiState(
    val height: String = "",
    val prePregnancyWeight: String = "",
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel // <-- 1. Anota a classe para o Hilt
class WeightProfileViewModel @Inject constructor( // <-- 2. Adiciona @Inject e o construtor
    private val repository: WeightRepository // <-- 3. Recebe o repositório como parâmetro
) : ViewModel() {

    // 4. A linha "private val repository = WeightRepository()" foi removida.

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
            // Usa o repositório que foi injetado pelo Hilt
            val profile = repository.getWeightProfile(userId)
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
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
                // Usa o repositório injetado
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