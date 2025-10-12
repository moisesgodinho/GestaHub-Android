package br.com.gestahub.ui.home

import androidx.lifecycle.ViewModel
import br.com.gestahub.data.GestationalProfileRepository
import br.com.gestahub.data.WeeklyInfo
import br.com.gestahub.data.WeeklyInfoProvider
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

sealed class GestationalDataState {
    object Loading : GestationalDataState()
    object NoData : GestationalDataState()
    data class HasData(
        val gestationalWeeks: Int,
        val gestationalDays: Int,
        val dueDate: String,
        val countdownWeeks: Int,
        val countdownDays: Int,
        val gestationalData: GestationalData,
        val weeklyInfo: WeeklyInfo?,
        val estimatedLmp: LocalDate?
    ) : GestationalDataState()
}

data class GestationalData(
    val lmp: String? = null,
    val ultrasoundExamDate: String? = null,
    val weeksAtExam: String? = null,
    val daysAtExam: String? = null
)

data class UiState(
    val dataState: GestationalDataState = GestationalDataState.Loading
)
class HomeViewModel : ViewModel() {

    private val repository = GestationalProfileRepository()
    private var gestationalDataListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun listenToGestationalData(userId: String) {
        gestationalDataListener?.remove()
        gestationalDataListener = repository.getGestationalProfileFlow(userId).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                _uiState.update { it.copy(dataState = GestationalDataState.NoData) }
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

            calculateUiState(rawData)
        }
    }

    private fun calculateUiState(data: GestationalData) {
        val lmpDate = data.lmp?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }
        val ultrasoundDate = data.ultrasoundExamDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }
        val weeks = data.weeksAtExam?.toIntOrNull() ?: 0
        val days = data.daysAtExam?.toIntOrNull() ?: 0

        val estimatedLmp = getEstimatedLmp(lmpDate, ultrasoundDate, weeks, days)

        if (estimatedLmp == null) {
            _uiState.update { it.copy(dataState = GestationalDataState.NoData) }
            return
        }

        // --- CORREÇÃO APLICADA AQUI ---
        // Trocamos 'LocalDate.now(ZoneId.of("UTC"))' por 'LocalDate.now()'
        // para usar a data local do celular, resolvendo a discrepância.
        val today = LocalDate.now()

        val gestationalAgeInDays = ChronoUnit.DAYS.between(estimatedLmp, today).toInt()
        val currentWeeks = gestationalAgeInDays / 7
        val currentDays = gestationalAgeInDays % 7

        val dueDate = estimatedLmp.plusDays(280)
        val remainingDays = ChronoUnit.DAYS.between(today, dueDate).toInt()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        // O cálculo da semana para o WeeklyInfo precisa de um ajuste para corresponder à contagem humana (semana 1 a 42)
        val weekForInfo = if (currentWeeks < 1) 1 else if (currentWeeks > 42) 42 else currentWeeks + 1
        val weeklyInfo = WeeklyInfoProvider.getInfoForWeek(weekForInfo)

        _uiState.update {
            it.copy(
                dataState = GestationalDataState.HasData(
                    gestationalWeeks = currentWeeks,
                    gestationalDays = currentDays,
                    dueDate = dueDate.format(dateFormatter),
                    countdownWeeks = if (remainingDays >= 0) remainingDays / 7 else 0,
                    countdownDays = if (remainingDays >= 0) remainingDays % 7 else 0,
                    gestationalData = data,
                    weeklyInfo = weeklyInfo,
                    estimatedLmp = estimatedLmp
                )
            )
        }
    }

    private fun getEstimatedLmp(lmp: LocalDate?, ultrasoundExamDate: LocalDate?, weeksAtExam: Int, daysAtExam: Int): LocalDate? {
        if (ultrasoundExamDate != null && (weeksAtExam > 0 || daysAtExam > 0)) {
            val daysAtExamTotal = (weeksAtExam * 7) + daysAtExam
            return ultrasoundExamDate.minusDays(daysAtExamTotal.toLong())
        }
        return lmp
    }

    override fun onCleared() {
        super.onCleared()
        gestationalDataListener?.remove()
    }
}