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
    val lmp: String? = null, // Alterado para String
    val ultrasoundExamDate: String? = null, // Alterado para String
    val weeksAtExam: String? = null, // Alterado para String
    val daysAtExam: String? = null // Alterado para String
)

data class UiState(
    val hasData: Boolean = false,
    val gestationalWeeks: Int = 0,
    val gestationalDays: Int = 0,
    val dueDate: String = "",
    val countdownWeeks: Int = 0,
    val countdownDays: Int = 0,
    val isLoading: Boolean = true,
    val gestationalData: GestationalData = GestationalData() // Adicionado
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

            val rawData = GestationalData(
                lmp = lmpString,
                ultrasoundExamDate = ultrasoundMap?.get("examDate") as? String,
                weeksAtExam = ultrasoundMap?.get("weeksAtExam") as? String,
                daysAtExam = ultrasoundMap?.get("daysAtExam") as? String
            )

            // Usar os dados brutos para os cálculos
            val gestationalDataForCalc = GestationalData(
                lmp = rawData.lmp,
                ultrasoundExamDate = rawData.ultrasoundExamDate,
                weeksAtExam = rawData.weeksAtExam,
                daysAtExam = rawData.daysAtExam
            )

            calculateUiState(gestationalDataForCalc, rawData)
        }
    }

    private fun calculateUiState(dataForCalc: GestationalData, rawData: GestationalData) {
        val lmpDate = dataForCalc.lmp?.let { LocalDate.parse(it) }
        val ultrasoundDate = dataForCalc.ultrasoundExamDate?.let { LocalDate.parse(it) }
        val weeks = dataForCalc.weeksAtExam?.toIntOrNull() ?: 0
        val days = dataForCalc.daysAtExam?.toIntOrNull() ?: 0

        val estimatedLmp = getEstimatedLmp(
            GestationalData( // Recriar com tipos corretos para o cálculo
                lmp = lmpDate?.toString(),
                ultrasoundExamDate = ultrasoundDate?.toString(),
                weeksAtExam = weeks.toString(),
                daysAtExam = days.toString()
            )
        )

        if (estimatedLmp == null) {
            _uiState.value = UiState(isLoading = false, hasData = false, gestationalData = rawData)
            return
        }

        val today = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
        val gestationalAgeInDays = ChronoUnit.DAYS.between(estimatedLmp, today).toInt()

        val currentWeeks = gestationalAgeInDays / 7
        val currentDays = gestationalAgeInDays % 7

        val dueDate = estimatedLmp.plusDays(280)
        val remainingDays = ChronoUnit.DAYS.between(today, dueDate).toInt()

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        _uiState.value = UiState(
            hasData = true,
            gestationalWeeks = currentWeeks,
            gestationalDays = currentDays,
            dueDate = dueDate.format(dateFormatter),
            countdownWeeks = if (remainingDays > 0) remainingDays / 7 else 0,
            countdownDays = if (remainingDays > 0) remainingDays % 7 else 0,
            isLoading = false,
            gestationalData = rawData // Salvar os dados brutos no state
        )
    }


    private fun getEstimatedLmp(data: GestationalData): LocalDate? {
        val ultrasoundExamDate = data.ultrasoundExamDate?.let { LocalDate.parse(it) }
        val weeksAtExam = data.weeksAtExam?.toIntOrNull() ?: 0
        val daysAtExam = data.daysAtExam?.toIntOrNull() ?: 0
        val lmp = data.lmp?.let { LocalDate.parse(it) }

        // Prioriza o ultrassom
        if (ultrasoundExamDate != null && weeksAtExam > 0) {
            val daysAtExamTotal = (weeksAtExam * 7) + daysAtExam
            return ultrasoundExamDate.minusDays(daysAtExamTotal.toLong())
        }
        // Se não, usa a DUM
        return lmp
    }
}