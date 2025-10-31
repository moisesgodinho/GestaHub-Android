// app/src/main/java/br/com/gestahub/ui/shoppinglist/ShoppingListViewModel.kt
package br.com.gestahub.ui.shoppinglist

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.getDefaultShoppingList
import kotlinx.coroutines.launch

class ShoppingListViewModel : ViewModel() {
    private val repository = ShoppingListRepository()

    var listData = mutableStateOf<Map<String, ShoppingListCategory>?>(null)
    var checkedItems = mutableStateOf<List<String>>(emptyList())
    var loading = mutableStateOf(true)

    init {
        loadShoppingList()
    }

    private fun loadShoppingList() {
        viewModelScope.launch {
            loading.value = true
            val data = repository.getShoppingListData()
            listData.value = data?.shoppingList
            checkedItems.value = data?.shoppingListChecked ?: emptyList()
            loading.value = false
        }
    }

    // Função auxiliar para atualizar o Firestore
    private fun updateFirestore() {
        viewModelScope.launch {
            val currentList = listData.value
            if (currentList != null) {
                val dataToSave = ShoppingListFirestore(
                    shoppingList = currentList,
                    shoppingListChecked = checkedItems.value
                )
                repository.updateShoppingListData(dataToSave)
            }
        }
    }

    fun toggleItem(itemId: String) {
        val currentChecked = checkedItems.value.toMutableList()
        if (currentChecked.contains(itemId)) {
            currentChecked.remove(itemId)
        } else {
            currentChecked.add(itemId)
        }
        checkedItems.value = currentChecked
        updateFirestore()
    }

    fun addItem(categoryId: String, label: String) {
        val currentList = listData.value?.toMutableMap() ?: return
        val category = currentList[categoryId] ?: return

        val newItem = ShoppingListItem(
            id = "custom-${System.currentTimeMillis()}",
            label = label,
            isCustom = true
        )

        val updatedItems = category.items + newItem
        currentList[categoryId] = category.copy(items = updatedItems)
        listData.value = currentList

        updateFirestore()
    }

    fun removeItem(categoryId: String, itemId: String) {
        val currentList = listData.value?.toMutableMap() ?: return
        val category = currentList[categoryId] ?: return

        val updatedItems = category.items.filter { it.id != itemId }
        currentList[categoryId] = category.copy(items = updatedItems)
        listData.value = currentList

        if (checkedItems.value.contains(itemId)) {
            checkedItems.value = checkedItems.value.filter { it != itemId }
        }

        updateFirestore()
    }

    fun restoreDefaults() {
        val currentList = listData.value?.toMutableMap() ?: return
        val defaultList = getDefaultShoppingList()
        var itemsRestored = 0

        val updatedList = currentList.toMutableMap()

        defaultList.forEach { (categoryId, defaultCategory) ->
            val userCategory = updatedList[categoryId]
            if (userCategory != null) {
                val userItemIds = userCategory.items.map { it.id }.toSet()
                val itemsToAdd = defaultCategory.items.filter { it.id !in userItemIds }
                if (itemsToAdd.isNotEmpty()) {
                    val combinedItems = userCategory.items + itemsToAdd
                    updatedList[categoryId] = userCategory.copy(items = combinedItems.sortedBy { it.id })
                    itemsRestored += itemsToAdd.size
                }
            } else {
                updatedList[categoryId] = defaultCategory
                itemsRestored += defaultCategory.items.size
            }
        }

        if (itemsRestored > 0) {
            listData.value = updatedList
            updateFirestore()
        }
    }
}