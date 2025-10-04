package br.com.gestahub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val displayName: String = "",
    val birthDate: String = "", // Formato "YYYY-MM-DD"
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class EditProfileViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val authUser = Firebase.auth.currentUser
    private val userId = authUser?.uid

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, displayName = authUser?.displayName ?: "") }
            return
        }
        val userDocRef = db.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { snapshot ->
            val personalProfile = snapshot.get("personalProfile") as? Map<*, *>
            _uiState.update {
                it.copy(
                    isLoading = false,
                    displayName = personalProfile?.get("displayName") as? String ?: authUser?.displayName ?: "",
                    birthDate = personalProfile?.get("birthDate") as? String ?: ""
                )
            }
        }.addOnFailureListener {
            _uiState.update { it.copy(isLoading = false, displayName = authUser?.displayName ?: "") }
        }
    }

    fun onDisplayNameChange(newName: String) {
        _uiState.update { it.copy(displayName = newName) }
    }

    fun onBirthDateChange(newDate: String) {
        _uiState.update { it.copy(birthDate = newDate) }
    }

    fun saveProfile() {
        if (userId == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val userDocRef = db.collection("users").document(userId)

            val personalProfile = hashMapOf(
                "displayName" to _uiState.value.displayName,
                "birthDate" to _uiState.value.birthDate
            )

            // Usamos `set` com `merge` para criar ou atualizar o documento/campo sem sobrescrever outros mapas como `gestationalProfile`
            userDocRef.set(hashMapOf("personalProfile" to personalProfile), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                .addOnFailureListener {
                    _uiState.update { it.copy(isSaving = false) }
                    // TODO: Mostrar mensagem de erro
                }
        }
    }
}