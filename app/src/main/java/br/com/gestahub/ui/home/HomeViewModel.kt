// Local: app/src/main/java/br/com/gestahub/ui/home/HomeViewModel.kt
package br.com.gestahub.ui.home

import androidx.lifecycle.ViewModel
import br.com.gestahub.data.GestationalProfileRepository
import br.com.gestahub.data.WeeklyInfo
import br.com.gestahub.data.WeeklyInfoProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import com.google.firebase.firestore.ListenerRegistration // <-- ADICIONE ESTE IMPORT

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
        // --- NOVA PROPRIEDADE ADICIONADA AQUI ---
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

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    // Variável para guardar o listener e poder removê-lo depois
    private var gestationalDataListener: ListenerRegistration? = null

    fun listenToGestationalData(userId: String) {
        // Remove qualquer listener anterior para evitar duplicidade ou vazamento de memória
        gestationalDataListener?.remove()

        try {
            // Usamos o userId recebido para buscar os dados
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
        } catch (e: Exception) {
            _uiState.update { it.copy(dataState = GestationalDataState.NoData) }
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

        val today = LocalDate.now(ZoneId.of("UTC"))
        val gestationalAgeInDays = ChronoUnit.DAYS.between(estimatedLmp, today).toInt()
        val currentWeeks = gestationalAgeInDays / 7
        val currentDays = gestationalAgeInDays % 7

        val dueDate = estimatedLmp.plusDays(280)
        val remainingDays = ChronoUnit.DAYS.between(today, dueDate).toInt()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        val weeklyInfo = WeeklyInfoProvider.getInfoForWeek(currentWeeks)

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
                    // --- VALOR DA NOVA PROPRIEDADE PASSADO AQUI ---
                    estimatedLmp = estimatedLmp
                )
            )
        }
    }

    private fun getEstimatedLmp(lmp: LocalDate?, ultrasoundExamDate: LocalDate?, weeksAtExam: Int, daysAtExam: Int): LocalDate? {
        if (ultrasoundExamDate != null && weeksAtExam > 0) {
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