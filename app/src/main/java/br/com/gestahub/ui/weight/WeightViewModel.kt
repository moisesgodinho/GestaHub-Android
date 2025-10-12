package br.com.gestahub.ui.weight

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.WeightRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.pow

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
    // --- ALTERADO AQUI ---
    // Uma lista para os pontos (peso) e outra para as legendas (datas)
    val weightChartEntries: List<FloatEntry> = emptyList(),
    val chartDateLabels: List<String> = emptyList()
)

class WeightViewModel(private val estimatedLmp: LocalDate?) : ViewModel() {
    private val repository = WeightRepository()
    private var weightListener: ListenerRegistration? = null
    private var profileListener: ListenerRegistration? = null
    private val authStateListener: FirebaseAuth.AuthStateListener

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
            calculateWeightSummary()
            updateWeightChartData()
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
                            val age = calculateGestationalAge(estimatedLmp, entryDate)
                            ages[entry.date] = "${age.weeks}s ${age.days}d"
                        } catch (e: Exception) {
                            // Ignora entradas com data mal formatada
                        }
                    }
                }

                _uiState.update { it.copy(isLoading = false, entries = entries, gestationalAges = ages) }
                calculateWeightSummary()
                updateWeightChartData()
            } catch (e: Exception) {
                Log.e("WeightViewModel", "FALHA AO CONVERTER DADOS DO FIREBASE!", e)
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao ler os dados salvos.") }
            }
        }
    }

    // --- LÓGICA DO GRÁFICO ATUALIZADA (BASEADA NO PWA) ---
    private fun updateWeightChartData() {
        val profile = _uiState.value.profile
        val entries = _uiState.value.entries

        if (estimatedLmp == null || profile == null || profile.prePregnancyWeight <= 0) {
            _uiState.update { it.copy(weightChartEntries = emptyList(), chartDateLabels = emptyList()) }
            return
        }

        val chartEntries = mutableListOf<FloatEntry>()
        val dateLabels = mutableListOf<String>()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

        // 1. Ponto Inicial
        chartEntries.add(FloatEntry(0f, profile.prePregnancyWeight.toFloat()))
        dateLabels.add("Início")

        // 2. Histórico de Peso
        // Usamos um índice numérico simples para o eixo X, como no Chart.js
        entries.sortedBy { it.date }.forEachIndexed { index, entry ->
            chartEntries.add(FloatEntry(index + 1f, entry.weight.toFloat()))
            try {
                dateLabels.add(LocalDate.parse(entry.date).format(dateFormatter))
            } catch (e: Exception) {
                dateLabels.add("-")
            }
        }

        // 3. Ponto Final (DPP)
        val dueDate = estimatedLmp.plusDays(280)
        if (dueDate.isAfter(LocalDate.now())) {
            val lastWeight = entries.firstOrNull()?.weight?.toFloat() ?: profile.prePregnancyWeight.toFloat()
            chartEntries.add(FloatEntry(chartEntries.size.toFloat(), lastWeight))
            dateLabels.add("DPP")
        }

        _uiState.update {
            it.copy(weightChartEntries = chartEntries, chartDateLabels = dateLabels)
        }
    }


    private data class GestationalAge(val weeks: Int, val days: Int)
    private fun calculateGestationalAge(lmp: LocalDate, targetDate: LocalDate): GestationalAge {
        val daysBetween = ChronoUnit.DAYS.between(lmp, targetDate).toInt()
        if (daysBetween < 0) return GestationalAge(0, 0)
        val weeks = daysBetween / 7
        val days = daysBetween % 7
        return GestationalAge(weeks, days)
    }

    private fun calculateWeightSummary() {
        val profile = _uiState.value.profile
        val entries = _uiState.value.entries
        if (profile == null || profile.height <= 0 || profile.prePregnancyWeight <= 0) {
            return
        }

        val heightInMeters = profile.height / 100.0
        val initialWeight = profile.prePregnancyWeight
        val latestWeight = entries.firstOrNull()?.weight ?: initialWeight

        val initialBmi = initialWeight / heightInMeters.pow(2)
        val currentBmi = latestWeight / heightInMeters.pow(2)
        val totalGain = latestWeight - initialWeight

        val gainGoal = when {
            initialBmi < 18.5 -> "12.5 - 18.0 kg"
            initialBmi < 25.0 -> "11.5 - 16.0 kg"
            initialBmi < 30.0 -> "7.0 - 11.5 kg"
            else -> "5.0 - 9.0 kg"
        }

        _uiState.update {
            it.copy(
                initialBmi = initialBmi,
                currentBmi = currentBmi,
                totalGain = totalGain,
                gainGoal = gainGoal
            )
        }
    }

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