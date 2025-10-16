package br.com.gestahub.ui.maternitybag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.MaternityBagData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaternityBagUiState(
    val listData: MaternityBagList? = null,
    val checkedItems: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MaternityBagViewModel : ViewModel() {
    private val repository = MaternityBagRepository()

    private val _uiState = MutableStateFlow(MaternityBagUiState())
    val uiState: StateFlow<MaternityBagUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMaternityBagData().collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _uiState.update {
                            it.copy(
                                listData = data.maternityBagList,
                                checkedItems = data.maternityBagChecked,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(error = error.message ?: "Erro desconhecido", isLoading = false)
                        }
                    }
                )
            }
        }
    }

    private suspend fun updateFirestore() {
        val currentState = _uiState.value
        if (currentState.listData != null) {
            repository.updateMaternityBag(
                MaternityBagFirestore(
                    maternityBagList = currentState.listData,
                    maternityBagChecked = currentState.checkedItems
                )
            )
        }
    }

    fun toggleItem(itemId: String) {
        val currentChecked = _uiState.value.checkedItems
        val newCheckedItems = if (currentChecked.contains(itemId)) {
            currentChecked.filter { it != itemId }
        } else {
            currentChecked + itemId
        }
        _uiState.update { it.copy(checkedItems = newCheckedItems) }
        viewModelScope.launch {
            updateFirestore()
        }
    }

    // As funções addItem, removeItem e restoreDefaults serão adicionadas na próxima etapa
    // para focar primeiro na visualização.
}