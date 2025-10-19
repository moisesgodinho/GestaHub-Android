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