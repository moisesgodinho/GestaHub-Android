// app/src/main/java/br/com/gestahub/ui/shoppinglist/ShoppingListModels.kt
package br.com.gestahub.ui.shoppinglist

data class ShoppingListItem(
    val id: String = "",
    val label: String = "",
    val isCustom: Boolean = false
)

data class ShoppingListCategory(
    val title: String = "",
    val items: List<ShoppingListItem> = emptyList()
)

// NOVO: Classe que representa o documento no Firestore
data class ShoppingListFirestore(
    val shoppingList: Map<String, ShoppingListCategory> = emptyMap(),
    val shoppingListChecked: List<String> = emptyList()
)