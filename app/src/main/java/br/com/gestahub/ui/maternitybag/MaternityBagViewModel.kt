package br.com.gestahub.ui.maternitybag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.MaternityBagData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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

    fun addItem(categoryId: String, label: String) {
        val currentState = _uiState.value
        if (currentState.listData == null || label.isBlank()) return

        val newItem = MaternityBagItem(
            id = "custom-${UUID.randomUUID()}",
            label = label,
            isCustom = true
        )

        val newListData = currentState.listData.let {
            val momItems = if (categoryId == "mom") it.mom.items + newItem else it.mom.items
            val babyItems = if (categoryId == "baby") it.baby.items + newItem else it.baby.items
            val companionItems = if (categoryId == "companion") it.companion.items + newItem else it.companion.items
            val docsItems = if (categoryId == "docs") it.docs.items + newItem else it.docs.items

            it.copy(
                mom = it.mom.copy(items = momItems),
                baby = it.baby.copy(items = babyItems),
                companion = it.companion.copy(items = companionItems),
                docs = it.docs.copy(items = docsItems)
            )
        }

        _uiState.update { it.copy(listData = newListData) }
        viewModelScope.launch { updateFirestore() }
    }

    fun removeItem(categoryId: String, itemId: String) {
        val currentState = _uiState.value
        if (currentState.listData == null) return

        val newListData = currentState.listData.let {
            val momItems = if (categoryId == "mom") it.mom.items.filter { item -> item.id != itemId } else it.mom.items
            val babyItems = if (categoryId == "baby") it.baby.items.filter { item -> item.id != itemId } else it.baby.items
            val companionItems = if (categoryId == "companion") it.companion.items.filter { item -> item.id != itemId } else it.companion.items
            val docsItems = if (categoryId == "docs") it.docs.items.filter { item -> item.id != itemId } else it.docs.items

            it.copy(
                mom = it.mom.copy(items = momItems),
                baby = it.baby.copy(items = babyItems),
                companion = it.companion.copy(items = companionItems),
                docs = it.docs.copy(items = docsItems)
            )
        }

        val newCheckedItems = currentState.checkedItems.filter { it != itemId }

        _uiState.update { it.copy(listData = newListData, checkedItems = newCheckedItems) }
        viewModelScope.launch { updateFirestore() }
    }

    /**
     * Adiciona de volta apenas os itens padrão que foram deletados,
     * mantendo os itens personalizados criados pela usuária.
     */
    fun restoreMissingDefaults() {
        val currentState = _uiState.value
        val userList = currentState.listData ?: return
        val defaultData = MaternityBagData.defaultData

        fun mergeItems(userItems: List<MaternityBagItem>, defaultItems: List<MaternityBagItem>): List<MaternityBagItem> {
            val userItemIds = userItems.map { it.id }.toSet()
            val missingDefaultItems = defaultItems.filter { !userItemIds.contains(it.id) }
            return userItems + missingDefaultItems
        }

        val newListData = userList.copy(
            mom = userList.mom.copy(items = mergeItems(userList.mom.items, defaultData.mom.items)),
            baby = userList.baby.copy(items = mergeItems(userList.baby.items, defaultData.baby.items)),
            companion = userList.companion.copy(items = mergeItems(userList.companion.items, defaultData.companion.items)),
            docs = userList.docs.copy(items = mergeItems(userList.docs.items, defaultData.docs.items))
        )

        _uiState.update { it.copy(listData = newListData) }
        viewModelScope.launch { updateFirestore() }
    }

    /**
     * Limpa completamente a lista atual e a substitui pela lista padrão.
     * Itens personalizados são apagados.
     */
    fun resetToDefaults() {
        val defaultData = MaternityBagData.defaultData
        _uiState.update { it.copy(listData = defaultData, checkedItems = emptyList()) }
        viewModelScope.launch { updateFirestore() }
    }
}