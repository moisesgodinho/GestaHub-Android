package br.com.gestahub.data

import br.com.gestahub.ui.weight.WeightEntry
import br.com.gestahub.ui.weight.WeightProfile
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class WeightRepository {
    private val db = Firebase.firestore

    /**
     * Busca o perfil de peso do usuário (altura, etc.).
     */
    suspend fun getWeightProfile(userId: String): WeightProfile? {
        return try {
            db.collection("users").document(userId)
                .collection("weightProfile").document("profile")
                .get().await().toObject(WeightProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retorna uma query que observa o histórico de peso de um usuário.
     */
    fun getWeightHistoryFlow(userId: String): Query {
        return db.collection("users").document(userId)
            .collection("weightHistory")
            .orderBy("date", Query.Direction.DESCENDING)
    }

    /**
     * Salva ou atualiza um registro de peso para uma data específica.
     * Usa .set() para garantir um único registro por dia.
     */
    suspend fun saveWeightEntry(userId: String, entry: WeightEntry) {
        val entryId = entry.date // A data "YYYY-MM-DD" é o ID do documento
        db.collection("users").document(userId)
            .collection("weightHistory")
            .document(entryId)
            .set(entry)
            .await()
    }

    /**
     * Deleta um registro de peso usando a data como ID.
     */
    suspend fun deleteWeightEntry(userId: String, entryDate: String) {
        db.collection("users").document(userId)
            .collection("weightHistory")
            .document(entryDate)
            .delete()
            .await()
    }
}