// app/src/main/java/br/com/gestahub/ui/shoppinglist/ShoppingListRepository.kt
package br.com.gestahub.ui.shoppinglist

import br.com.gestahub.data.getDefaultShoppingList
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ShoppingListRepository {
    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    // Nova função para obter a referência do documento 'main' na subcoleção 'shoppingList'
    private fun getShoppingListDocRef() = userId?.let {
        db.collection("users").document(it).collection("shoppingList").document("main")
    }

    suspend fun getShoppingListData(): ShoppingListFirestore? {
        val docRef = getShoppingListDocRef() ?: return null
        val docSnap = docRef.get().await()

        return if (docSnap.exists()) {
            // Se o documento existe, converte para o nosso objeto
            docSnap.toObject<ShoppingListFirestore>()
        } else {
            // Se não existe, cria um com os dados padrão, salva e retorna
            val defaultData = ShoppingListFirestore(
                shoppingList = getDefaultShoppingList(),
                shoppingListChecked = emptyList()
            )
            docRef.set(defaultData).await()
            defaultData
        }
    }

    // Função única para atualizar todos os dados da lista de uma vez
    suspend fun updateShoppingListData(data: ShoppingListFirestore) {
        getShoppingListDocRef()?.set(data)?.await()
    }
}