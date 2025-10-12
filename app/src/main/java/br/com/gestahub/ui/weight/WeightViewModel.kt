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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

data class WeightUiState(
    val profile: WeightProfile? = null,
    val entries: List<WeightEntry> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val initialBmi: Double = 0.0,
    val currentBmi: Double = 0.0,
    val totalGain: Double = 0.0,
    val gainGoal: String = ""
)

class WeightViewModel : ViewModel() {
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
                // --- CORREÇÃO APLICADA AQUI ---
                // Trocamos a conversão automática 'toObjects<WeightEntry>()' por um loop manual.
                // Isso é mais seguro e resolve o erro de compilação.
                val entries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<WeightEntry>()
                } ?: emptyList()

                _uiState.update { it.copy(isLoading = false, entries = entries) }
                calculateWeightSummary()
            } catch (e: Exception) {
                Log.e("WeightViewModel", "FALHA AO CONVERTER DADOS DO FIREBASE!", e)
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao ler os dados salvos.") }
            }
        }
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