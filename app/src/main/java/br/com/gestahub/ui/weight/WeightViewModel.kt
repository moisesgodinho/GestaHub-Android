package br.com.gestahub.ui.weight

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.WeightRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeightUiState(
    val entries: List<WeightEntry> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class WeightViewModel : ViewModel() {
    private val repository = WeightRepository()
    private var weightListener: ListenerRegistration? = null
    // O "ouvinte" que vai esperar pela confirmação do login
    private val authStateListener: FirebaseAuth.AuthStateListener

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Ponto chave da correção: O ViewModel agora "ouve" o estado de autenticação.
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // A busca de dados só acontece DEPOIS que o Firebase confirma o usuário.
                listenToWeightHistory(user.uid)
            } else {
                // Se o usuário deslogar, limpa a tela e o listener de dados.
                weightListener?.remove()
                _uiState.update { it.copy(isLoading = false, entries = emptyList()) }
            }
        }
        // Ativa o "ouvinte"
        Firebase.auth.addAuthStateListener(authStateListener)
    }

    private fun listenToWeightHistory(userId: String) {
        _uiState.update { it.copy(isLoading = true) }
        weightListener?.remove() // Previne listeners duplicados
        weightListener = repository.getWeightHistoryFlow(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao carregar o histórico.") }
                return@addSnapshotListener
            }
            try {
                val entries = snapshot?.toObjects<WeightEntry>() ?: emptyList()
                _uiState.update { it.copy(isLoading = false, entries = entries) }
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Falha ao converter dados do Firestore!", e)
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao ler os dados salvos.") }
            }
        }
    }

    fun deleteWeightEntry(entry: WeightEntry) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.deleteWeightEntry(userId, entry.id)
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
        // Limpa todos os listeners quando o ViewModel é destruído para evitar vazamentos de memória.
        Firebase.auth.removeAuthStateListener(authStateListener)
        weightListener?.remove()
    }
}