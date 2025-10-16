package br.com.gestahub.ui.maternitybag

import br.com.gestahub.data.MaternityBagData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MaternityBagRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getUserDocRef() = auth.currentUser?.uid?.let { db.collection("users").document(it) }

    fun getMaternityBagData(): Flow<Result<MaternityBagFirestore>> = callbackFlow {
        val userDocRef = getUserDocRef()
        if (userDocRef == null) {
            trySend(Result.failure(Exception("Usuário não autenticado.")))
            return@callbackFlow
        }

        val listener = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val gestationalProfile = snapshot.get("gestationalProfile") as? Map<*, *>
                val list = gestationalProfile?.get("maternityBagList")

                if (list != null) {
                    val bagData = snapshot.get("gestationalProfile")?.let {
                        val map = it as HashMap<String, Any>
                        // Manual conversion is more robust here
                        val bagListMap = map["maternityBagList"] as? HashMap<String, Any> ?: hashMapOf()
                        val checkedList = map["maternityBagChecked"] as? List<String> ?: emptyList()

                        // Helper to convert map to Category
                        fun mapToCategory(categoryMap: Any?): MaternityBagCategory {
                            if (categoryMap !is Map<*, *>) return MaternityBagCategory()
                            val title = categoryMap["title"] as? String ?: ""
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

                        MaternityBagFirestore(bagList, checkedList)
                    } ?: MaternityBagFirestore()
                    trySend(Result.success(bagData))
                } else {
                    // Se não existe, cria a lista padrão
                    val default = MaternityBagData.defaultData
                    userDocRef.set(
                        mapOf("gestationalProfile" to mapOf("maternityBagList" to default)),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    // O listener será acionado novamente com os novos dados
                }
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateMaternityBag(data: MaternityBagFirestore) {
        getUserDocRef()?.set(
            mapOf("gestationalProfile" to data),
            com.google.firebase.firestore.SetOptions.merge()
        )?.await()
    }
}