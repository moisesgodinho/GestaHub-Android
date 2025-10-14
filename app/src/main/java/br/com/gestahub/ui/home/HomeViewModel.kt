package br.com.gestahub.ui.home

import androidx.lifecycle.ViewModel
import br.com.gestahub.data.GestationalProfileRepository
import br.com.gestahub.data.WeeklyInfo
import br.com.gestahub.domain.usecase.CalculateGestationalInfoUseCase
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

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
    private val calculateGestationalInfoUseCase = CalculateGestationalInfoUseCase()

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

            processGestationalData(rawData)
        }
    }

    private fun processGestationalData(data: GestationalData) {
        val gestationalInfo = calculateGestationalInfoUseCase(data)

        if (gestationalInfo == null) {
            _uiState.update { it.copy(dataState = GestationalDataState.NoData) }
            return
        }

        _uiState.update {
            it.copy(
                dataState = GestationalDataState.HasData(
                    gestationalWeeks = gestationalInfo.gestationalWeeks,
                    gestationalDays = gestationalInfo.gestationalDays,
                    dueDate = gestationalInfo.dueDate,
                    countdownWeeks = gestationalInfo.countdownWeeks,
                    countdownDays = gestationalInfo.countdownDays,
                    gestationalData = data,
                    weeklyInfo = gestationalInfo.weeklyInfo,
                    estimatedLmp = gestationalInfo.estimatedLmp
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        gestationalDataListener?.remove()
    }
}