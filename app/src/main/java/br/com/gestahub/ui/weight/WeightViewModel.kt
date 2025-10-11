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

data class WeightUiState(
    val entries: List<WeightEntry> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class WeightViewModel : ViewModel() {
    private val repository = WeightRepository()
    private var weightListener: ListenerRegistration? = null
    private val authStateListener: FirebaseAuth.AuthStateListener

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState = _uiState.asStateFlow()

    init {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenToWeightHistory(user.uid)
            } else {
                weightListener?.remove()
                _uiState.update { it.copy(isLoading = false, entries = emptyList()) }
            }
        }
        Firebase.auth.addAuthStateListener(authStateListener)
    }

    private fun listenToWeightHistory(userId: String) {
        _uiState.update { it.copy(isLoading = true) }
        weightListener?.remove()
        weightListener = repository.getWeightHistoryFlow(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, userMessage = "Erro ao carregar o histórico.") }
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val weightEntries = mutableListOf<WeightEntry>()
                for (document in snapshot.documents) {
                    try {
                        // A conversão agora usa o novo modelo WeightEntry
                        val entry = document.toObject<WeightEntry>()
                        if (entry != null) {
                            weightEntries.add(entry)
                        }
                    } catch (e: Exception) {
                        Log.e("WeightViewModel", "Falha ao converter o documento ${document.id}", e)
                    }
                }
                _uiState.update { it.copy(isLoading = false, entries = weightEntries) }
            } else {
                _uiState.update { it.copy(isLoading = false, entries = emptyList()) }
            }
        }
    }

    fun deleteWeightEntry(entry: WeightEntry) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Agora deleta usando a data como ID
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
    }
}