package br.com.gestahub.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.com.gestahub.workers.NotificationWorker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val age: String = "Não definida"
)

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val db = Firebase.firestore
    private val authUser = Firebase.auth.currentUser
    private val userId = authUser?.uid

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                userProfile = UserProfile(
                    displayName = authUser?.displayName ?: "Carregando...",
                    email = authUser?.email ?: "Carregando..."
                )
            )
        }
        listenToUserProfile()
    }

    private fun listenToUserProfile() {
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        val userDocRef = db.collection("users").document(userId)
        userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = UserProfile(
                            displayName = authUser?.displayName ?: "Usuário",
                            email = authUser?.email ?: "Não definido"
                        )
                    )
                }
                return@addSnapshotListener
            }

            val profile = snapshot.get("profile") as? Map<*, *>
            val displayName = profile?.get("displayName") as? String ?: authUser?.displayName
            val birthDate = profile?.get("dob") as? String

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userProfile = UserProfile(
                        displayName = displayName ?: "Não definido",
                        email = authUser?.email ?: "Não definido",
                        age = calculateAge(birthDate)
                    )
                )
            }
        }
    }

    private fun calculateAge(birthDateString: String?): String {
        if (birthDateString.isNullOrBlank()) {
            return "Não definida"
        }
        return try {
            val birthDate = LocalDate.parse(birthDateString)
            val age = Period.between(birthDate, LocalDate.now()).years
            "$age anos"
        } catch (e: DateTimeParseException) {
            "Data inválida"
        }
    }

    fun testAppointmentReminder(context: Context) {
        val testRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(context).enqueue(testRequest)
    }

    fun signOut() {
        Firebase.auth.signOut()
    }
}