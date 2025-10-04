// ui/home/HomeViewModel.kt
package br.com.gestahub.ui.home

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

data class GestationalData(
    val lmp: LocalDate? = null,
    val ultrasoundExamDate: LocalDate? = null,
    val weeksAtExam: Int = 0,
    val daysAtExam: Int = 0
)

data class UiState(
    val hasData: Boolean = false,
    val gestationalWeeks: Int = 0,
    val gestationalDays: Int = 0,
    val dueDate: String = "",
    val countdownWeeks: Int = 0,
    val countdownDays: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenToGestationalData()
    }

    private fun listenToGestationalData() {
        if (userId == null) {
            _uiState.value = UiState(isLoading = false, hasData = false)
            return
        }

        val userDocRef = db.collection("users").document(userId)
        userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                _uiState.value = UiState(isLoading = false, hasData = false)
                return@addSnapshotListener
            }

            val profile = snapshot.get("gestationalProfile") as? Map<*, *>
            val lmpString = profile?.get("lmp") as? String
            val ultrasoundMap = profile?.get("ultrasound") as? Map<*, *>

            val gestationalData = GestationalData(
                lmp = lmpString?.let { LocalDate.parse(it) },
                ultrasoundExamDate = (ultrasoundMap?.get("examDate") as? String)?.let { LocalDate.parse(it) },
                weeksAtExam = (ultrasoundMap?.get("weeksAtExam") as? String)?.toIntOrNull() ?: 0,
                daysAtExam = (ultrasoundMap?.get("daysAtExam") as? String)?.toIntOrNull() ?: 0
            )

            calculateUiState(gestationalData)
        }
    }

    private fun calculateUiState(data: GestationalData) {
        val estimatedLmp = getEstimatedLmp(data)

        if (estimatedLmp == null) {
            _uiState.value = UiState(isLoading = false, hasData = false)
            return
        }

        val today = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
        val gestationalAgeInDays = ChronoUnit.DAYS.between(estimatedLmp, today).toInt()

        val weeks = gestationalAgeInDays / 7
        val days = gestationalAgeInDays % 7

        val dueDate = estimatedLmp.plusDays(280)
        val remainingDays = ChronoUnit.DAYS.between(today, dueDate).toInt()

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        _uiState.value = UiState(
            hasData = true,
            gestationalWeeks = weeks,
            gestationalDays = days,
            dueDate = dueDate.format(dateFormatter),
            countdownWeeks = if (remainingDays > 0) remainingDays / 7 else 0,
            countdownDays = if (remainingDays > 0) remainingDays % 7 else 0,
            isLoading = false
        )
    }

    private fun getEstimatedLmp(data: GestationalData): LocalDate? {
        // Prioriza o ultrassom
        if (data.ultrasoundExamDate != null && data.weeksAtExam > 0) {
            val daysAtExamTotal = (data.weeksAtExam * 7) + data.daysAtExam
            return data.ultrasoundExamDate.minusDays(daysAtExamTotal.toLong())
        }
        // Se não, usa a DUM
        return data.lmp
    }
}