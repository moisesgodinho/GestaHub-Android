package br.com.gestahub.data

import br.com.gestahub.ui.weight.WeightEntry
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Classe responsável pela comunicação com a coleção 'weightHistory' no Firestore.
 */
class WeightRepository {
    private val db = Firebase.firestore

    /**
     * Retorna uma query que observa o histórico de peso de um usuário.
     */
    fun getWeightHistoryFlow(userId: String): Query {
        return db.collection("users").document(userId)
            .collection("weightHistory")
            .orderBy("date", Query.Direction.DESCENDING)
    }

    /**
     * Adiciona um novo registro de peso.
     */
    suspend fun addWeightEntry(userId: String, entry: WeightEntry) {
        db.collection("users").document(userId)
            .collection("weightHistory")
            .add(entry)
            .await()
    }

    /**
     * Deleta um registro de peso.
     */
    suspend fun deleteWeightEntry(userId: String, entryId: String) {
        db.collection("users").document(userId)
            .collection("weightHistory")
            .document(entryId)
            .delete()
            .await()
    }
}