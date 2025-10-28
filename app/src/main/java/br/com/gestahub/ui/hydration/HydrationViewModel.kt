package br.com.gestahub.ui.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

data class HydrationUiState(
    val todayData: WaterIntakeEntry = WaterIntakeEntry(),
    val history: List<WaterIntakeEntry> = emptyList(),
    val displayedMonth: YearMonth = YearMonth.now(), // Novo estado para o mês do gráfico
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val repository: HydrationRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HydrationUiState())
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    init {
        listenToTodayData()
        listenToHistoryData()
    }

    private fun listenToTodayData() {
        viewModelScope.launch {
            repository.listenToTodayWaterIntake().collect { result ->
                result.fold(
                    onSuccess = { todayEntry ->
                        if (todayEntry != null) {
                            _uiState.update { it.copy(todayData = todayEntry, isLoading = false) }
                        } else {
                            viewModelScope.launch {
                                val profileGoal = getProfileWaterGoal()
                                val profileCupSize = getProfileWaterCupSize()
                                val todayId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val newTodayEntry = WaterIntakeEntry(
                                    id = todayId,
                                    goal = profileGoal,
                                    cupSize = profileCupSize,
                                    date = todayId
                                )
                                _uiState.update { it.copy(todayData = newTodayEntry, isLoading = false) }
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message, isLoading = false) }
                    }
                )
            }
        }
    }

    private fun listenToHistoryData() {
        viewModelScope.launch {
            repository.getWaterIntakeHistory().collect { result ->
                result.fold(
                    onSuccess = { historyList ->
                        val todayId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val filteredHistory = historyList.filter { entry ->
                            entry.date != null && entry.date <= todayId
                        }
                        _uiState.update { it.copy(history = filteredHistory) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
            }
        }
    }

    // Nova função para alterar o mês exibido
    fun changeDisplayedMonth(newMonth: YearMonth) {
        _uiState.update { it.copy(displayedMonth = newMonth) }
    }

    fun addWater() {
        val currentData = _uiState.value.todayData
        addCustomAmount(currentData.cupSize)
    }

    fun addCustomAmount(amount: Int) {
        if (amount <= 0 || _uiState.value.isLoading || _uiState.value.todayData.id.isBlank()) return
        val currentData = _uiState.value.todayData
        val newAmount = currentData.current + amount
        val newHistory = currentData.history + amount
        val newData = currentData.copy(current = newAmount, history = newHistory, date = currentData.id)
        _uiState.update { it.copy(todayData = newData) }
        viewModelScope.launch { repository.updateWaterData(newData) }
    }

    fun undoLastWater() {
        if (_uiState.value.isLoading || _uiState.value.todayData.id.isBlank()) return
        val currentData = _uiState.value.todayData
        if (currentData.history.isEmpty()) return
        val lastAmount = currentData.history.last()
        val newAmount = (currentData.current - lastAmount).coerceAtLeast(0)
        val newHistory = currentData.history.dropLast(1)
        val newData = currentData.copy(current = newAmount, history = newHistory, date = currentData.id)
        _uiState.update { it.copy(todayData = newData) }
        viewModelScope.launch { repository.updateWaterData(newData) }
    }

    fun setWaterSettings(newGoal: Int, newCupSize: Int) {
        if (newGoal <= 0 || newCupSize <= 0) return
        val currentData = _uiState.value.todayData
        val newData = currentData.copy(goal = newGoal, cupSize = newCupSize)
        _uiState.update { it.copy(todayData = newData) }
        viewModelScope.launch {
            repository.updateWaterData(newData)
            repository.updateProfileWaterSettings(newGoal, newCupSize)
        }
    }

    private suspend fun getProfileWaterGoal(): Int {
        return try {
            val docRef = auth.currentUser?.uid?.let { db.collection("users").document(it) }
            val document = docRef?.get()?.await()
            (document?.get("gestationalProfile.waterGoal") as? Long)?.toInt() ?: 2500
        } catch (e: Exception) {
            2500
        }
    }

    private suspend fun getProfileWaterCupSize(): Int {
        return try {
            val docRef = auth.currentUser?.uid?.let { db.collection("users").document(it) }
            val document = docRef?.get()?.await()
            (document?.get("gestationalProfile.waterCupSize") as? Long)?.toInt() ?: 250
        } catch (e: Exception) {
            250
        }
    }
}