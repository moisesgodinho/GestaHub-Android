package br.com.gestahub.ui.shoppinglist

import br.com.gestahub.data.getDefaultShoppingList
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ShoppingListRepository {
    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    @Suppress("UNCHECKED_CAST")
    suspend fun getShoppingListData(): Pair<Map<String, ShoppingListCategory>, List<String>>? {
        if (userId == null) return null
        val docRef = db.collection("users").document(userId)
        val docSnap = docRef.get().await()

        val gestationalProfile = docSnap.get("gestationalProfile") as? Map<String, Any>
        val shoppingList = gestationalProfile?.get("shoppingList") as? Map<String, Any>
        val checkedItems = gestationalProfile?.get("shoppingListChecked") as? List<String> ?: emptyList()

        // Se a lista de compras não existir (seja por ser um novo usuário ou por outro motivo),
        // cria a lista padrão e a retorna imediatamente.
        if (shoppingList == null) {
            val defaultList = getDefaultShoppingList()
            docRef.set(
                mapOf("gestationalProfile" to mapOf("shoppingList" to defaultList)),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            // Retorna a lista padrão recém-criada com uma lista de checados vazia.
            return Pair(defaultList, emptyList())
        } else {
            // Se a lista já existe, processa e a retorna.
            val mappedList = shoppingList.mapValues { (_, value) ->
                val categoryMap = value as Map<String, Any>
                ShoppingListCategory(
                    title = categoryMap["title"] as String,
                    items = (categoryMap["items"] as List<Map<String, Any>>).map { itemMap ->
                        ShoppingListItem(
                            id = itemMap["id"] as String,
                            label = itemMap["label"] as String,
                            isCustom = itemMap["isCustom"] as? Boolean ?: false
                        )
                    }
                )
            }
            return Pair(mappedList, checkedItems)
        }
    }

    suspend fun updateShoppingList(listData: Map<String, ShoppingListCategory>) {
        if (userId == null) return
        db.collection("users").document(userId).set(
            mapOf("gestationalProfile" to mapOf("shoppingList" to listData)),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun updateCheckedItems(checkedItems: List<String>) {
        if (userId == null) return
        db.collection("users").document(userId).set(
            mapOf("gestationalProfile" to mapOf("shoppingListChecked" to checkedItems)),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }
}