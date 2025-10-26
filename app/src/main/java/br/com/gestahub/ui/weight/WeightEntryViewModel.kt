package br.com.gestahub.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.WeightRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel // <-- ADICIONE ESTE IMPORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject // <-- ADICIONE ESTE IMPORT
import kotlin.math.pow

data class WeightEntryUiState(
    val date: Date,
    val weight: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel // <-- ADICIONE ESTA ANOTAÇÃO
class WeightEntryViewModel @Inject constructor( // <-- ADICIONE O @Inject E O CONSTRUTOR
    private val repository: WeightRepository // <-- RECEBE O REPOSITÓRIO COMO PARÂMETRO
) : ViewModel() {

    // REMOVA ESTA LINHA: private val repository = WeightRepository()
    private var userHeightCm: Int? = null

    private fun getStartOfToday(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private val _uiState = MutableStateFlow(WeightEntryUiState(date = getStartOfToday()))
    val uiState = _uiState.asStateFlow()

    init {
        loadUserHeight()
    }

    private fun loadUserHeight() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = repository.getWeightProfile(userId) // Usa o repositório injetado
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
            _uiState.update { it.copy(userMessage = "Não foi possível calcular o IMC. Verifique seu perfil de peso.") }
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
                val heightInMeters = heightInCm / 100.0
                val bmi = weightValue / heightInMeters.pow(2)

                val dateIdFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val dateId = dateIdFormatter.format(_uiState.value.date)

                val entry = WeightEntry(
                    date = dateId,
                    weight = weightValue,
                    bmi = bmi
                )

                repository.saveWeightEntry(userId, entry) // Usa o repositório injetado
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