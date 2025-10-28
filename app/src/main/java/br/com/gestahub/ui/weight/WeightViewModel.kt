package br.com.gestahub.ui.weight

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.WeightRepository
import br.com.gestahub.domain.usecase.CalculateGestationalAgeOnDateUseCase
import br.com.gestahub.domain.usecase.CalculateWeightSummaryUseCase
import br.com.gestahub.domain.usecase.GestationalAge
import br.com.gestahub.domain.usecase.PrepareWeightChartDataUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeightUiState(
    val profile: WeightProfile? = null,
    val entries: List<WeightEntry> = emptyList(),
    val gestationalAges: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val initialBmi: Double = 0.0,
    val currentBmi: Double = 0.0,
    val totalGain: Double = 0.0,
    val gainGoal: String = "",
    val weightChartEntries: List<SimpleChartEntry> = emptyList(),
    val chartDateLabels: List<String> = emptyList()
)

// O ViewModel agora recebe os Use Cases via construtor.
// Como não estamos usando Hilt neste ViewModel específico, a injeção é manual.
class WeightViewModel(private val estimatedLmp: LocalDate?) : ViewModel() {
    private val repository = WeightRepository()
    private var weightListener: ListenerRegistration? = null
    private var profileListener: ListenerRegistration? = null
    private val authStateListener: FirebaseAuth.AuthStateListener

    // --- MUDANÇA: Instanciando os Use Cases ---
    private val calculateGestationalAgeUseCase = CalculateGestationalAgeOnDateUseCase()
    private val calculateWeightSummaryUseCase = CalculateWeightSummaryUseCase()
    private val prepareWeightChartDataUseCase = PrepareWeightChartDataUseCase()
    // --- FIM DA MUDANÇA ---

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState = _uiState.asStateFlow()

    init {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenToWeightHistory(user.uid)
                listenToWeightProfile(user.uid)
            } else {
                weightListener?.remove()
                profileListener?.remove()
                _uiState.update { it.copy(isLoading = false, entries = emptyList(), profile = null) }
            }
        }
        Firebase.auth.addAuthStateListener(authStateListener)
    }

    private fun listenToWeightProfile(userId: String) {
        profileListener?.remove()
        profileListener = repository.addWeightProfileListener(userId) { profile ->
            _uiState.update { it.copy(profile = profile) }
            // Ao receber um novo perfil, recalcula tudo
            processAllData()
        }
    }

    private fun listenToWeightHistory(userId: String) {
        _uiState.update { it.copy(isLoading = true) }
        weightListener?.remove()
        weightListener = repository.getWeightHistoryFlow(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao carregar o histórico.") }
                return@addSnapshotListener
            }
            try {
                val entries = snapshot?.documents?.mapNotNull { it.toObject<WeightEntry>() } ?: emptyList()

                val ages = mutableMapOf<String, String>()
                if (estimatedLmp != null) {
                    entries.forEach { entry ->
                        try {
                            val entryDate = LocalDate.parse(entry.date)
                            val age : GestationalAge = calculateGestationalAgeUseCase(estimatedLmp, entryDate)
                            ages[entry.date] = "${age.weeks}s ${age.days}d"
                        } catch (e: Exception) { /* Ignora entradas com data mal formatada */ }
                    }
                }

                _uiState.update { it.copy(isLoading = false, entries = entries, gestationalAges = ages) }
                // Ao receber novos registros, recalcula tudo
                processAllData()
            } catch (e: Exception) {
                Log.e("WeightViewModel", "FALHA AO CONVERTER DADOS DO FIREBASE!", e)
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao ler os dados salvos.") }
            }
        }
    }

    // --- NOVO MÉTODO CENTRALIZADOR ---
    /**
     * Orquestra a chamada aos Use Cases e atualiza o estado da UI de uma só vez.
     */
    private fun processAllData() {
        val profile = _uiState.value.profile
        val entries = _uiState.value.entries

        // Calcula o sumário usando o Use Case
        val summary = calculateWeightSummaryUseCase(profile, entries)

        // Prepara os dados do gráfico usando o Use Case
        val chartData = prepareWeightChartDataUseCase(entries, profile, estimatedLmp)

        // Atualiza o estado da UI com os dados processados
        _uiState.update {
            it.copy(
                initialBmi = summary.initialBmi,
                currentBmi = summary.currentBmi,
                totalGain = summary.totalGain,
                gainGoal = summary.gainGoal,
                weightChartEntries = chartData.entries,
                chartDateLabels = chartData.labels
            )
        }
    }
    // --- FIM DO NOVO MÉTODO ---

    // --- ATENÇÃO: Os métodos updateWeightChartData() e calculateWeightSummary() foram REMOVIDOS ---
    // A lógica deles agora está nos Use Cases e é orquestrada pelo novo método processAllData().

    fun deleteWeightEntry(entry: WeightEntry) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.deleteWeightEntry(userId, entry.date)
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Erro ao excluir o registro.") }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Firebase.auth.removeAuthStateListener(authStateListener)
        weightListener?.remove()
        profileListener?.remove()
    }
}