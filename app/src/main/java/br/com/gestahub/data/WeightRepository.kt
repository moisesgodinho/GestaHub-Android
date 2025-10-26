package br.com.gestahub.data

import br.com.gestahub.ui.weight.WeightEntry
import br.com.gestahub.ui.weight.WeightProfile
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor() {
    private val db = Firebase.firestore

    /**
     * Adiciona um listener para observar em tempo real as mudanças no perfil de peso do usuário.
     */
    fun addWeightProfileListener(userId: String, onUpdate: (WeightProfile?) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId)
            .collection("weightProfile").document("profile")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    onUpdate(null)
                    return@addSnapshotListener
                }
                onUpdate(snapshot.toObject(WeightProfile::class.java))
            }
    }

    /**
     * Busca o perfil de peso do usuário uma única vez.
     * ESTA É A FUNÇÃO QUE ESTAVA FALTANDO E CAUSAVA O ERRO.
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
     * Salva o perfil de peso completo do usuário.
     */
    suspend fun saveWeightProfile(userId: String, profile: WeightProfile) {
        db.collection("users").document(userId)
            .collection("weightProfile").document("profile")
            .set(profile)
            .await()
    }

    fun getWeightHistoryFlow(userId: String): Query {
        return db.collection("users").document(userId)
            .collection("weightHistory")
            .orderBy("date", Query.Direction.DESCENDING)
    }

    suspend fun saveWeightEntry(userId: String, entry: WeightEntry) {
        val entryId = entry.date
        db.collection("users").document(userId)
            .collection("weightHistory")
            .document(entryId)
            .set(entry)
            .await()
    }

    suspend fun deleteWeightEntry(userId: String, entryDate: String) {
        db.collection("users").document(userId)
            .collection("weightHistory")
            .document(entryDate)
            .delete()
            .await()
    }
}