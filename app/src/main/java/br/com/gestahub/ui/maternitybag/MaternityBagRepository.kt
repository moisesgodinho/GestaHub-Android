// app/src/main/java/br/com/gestahub/ui/maternitybag/MaternityBagRepository.kt
package br.com.gestahub.ui.maternitybag

import br.com.gestahub.data.MaternityBagData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MaternityBagRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Nova função para obter a referência do documento 'main' na subcoleção 'maternityBag'
    private fun getMaternityBagDocRef() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("maternityBag").document("main")
    }

    fun getMaternityBagData(): Flow<Result<MaternityBagFirestore>> = callbackFlow {
        val bagDocRef = getMaternityBagDocRef()
        if (bagDocRef == null) {
            trySend(Result.failure(Exception("Usuário não autenticado.")))
            close()
            return@callbackFlow
        }

        val listener = bagDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            if (snapshot == null) {
                trySend(Result.failure(Exception("Snapshot nulo.")))
                return@addSnapshotListener
            }

            // Se o documento existe, processa os dados
            if (snapshot.exists()) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val bagListMap = snapshot.get("maternityBagList") as? HashMap<String, Any> ?: hashMapOf()
                    @Suppress("UNCHECKED_CAST")
                    val checkedList = snapshot.get("maternityBagChecked") as? List<String> ?: emptyList()

                    fun mapToCategory(categoryMap: Any?): MaternityBagCategory {
                        if (categoryMap !is Map<*, *>) return MaternityBagCategory()
                        val title = categoryMap["title"] as? String ?: ""
                        @Suppress("UNCHECKED_CAST")
                        val itemsList = categoryMap["items"] as? List<Map<String, Any>> ?: emptyList()
                        val items = itemsList.map { itemMap ->
                            MaternityBagItem(
                                id = itemMap["id"] as? String ?: "",
                                label = itemMap["label"] as? String ?: "",
                                isCustom = itemMap["isCustom"] as? Boolean ?: false
                            )
                        }
                        return MaternityBagCategory(title, items)
                    }

                    val bagList = MaternityBagList(
                        mom = mapToCategory(bagListMap["mom"]),
                        baby = mapToCategory(bagListMap["baby"]),
                        companion = mapToCategory(bagListMap["companion"]),
                        docs = mapToCategory(bagListMap["docs"])
                    )

                    val bagData = MaternityBagFirestore(bagList, checkedList)
                    trySend(Result.success(bagData))

                } catch (e: Exception) {
                    trySend(Result.failure(e))
                }
            } else {
                // Se o documento não existe, cria-o com os dados padrão
                val default = MaternityBagFirestore(
                    maternityBagList = MaternityBagData.defaultData,
                    maternityBagChecked = emptyList()
                )
                bagDocRef.set(default)
                // O listener será acionado novamente com os novos dados, então não precisamos enviar nada aqui.
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateMaternityBag(data: MaternityBagFirestore) {
        // Atualiza o documento 'main' diretamente com o objeto MaternityBagFirestore
        getMaternityBagDocRef()?.set(data)?.await()
    }
}