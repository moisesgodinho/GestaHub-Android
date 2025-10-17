package br.com.gestahub.ui.hydration

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HydrationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getWaterIntakeCollection() =
        auth.currentUser?.uid?.let { db.collection("users").document(it).collection("waterIntake") }

    fun getWaterHistory(): Flow<Result<List<WaterIntakeEntry>>> = callbackFlow {
        val collection = getWaterIntakeCollection()
        if (collection == null) {
            trySend(Result.failure(Exception("Usuário não autenticado.")))
            return@callbackFlow
        }

        // Ordenar pelo ID do documento, que é a data em formato yyyy-MM-dd, de forma decrescente
        val listener = collection
            .orderBy(com.google.firebase.firestore.FieldPath.documentId(), Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val goal = (doc.getLong("goal") ?: 2500L).toInt()
                            val current = (doc.getLong("current") ?: 0L).toInt()
                            val cupSize = (doc.getLong("cupSize") ?: 250L).toInt()

                            // --- CORREÇÃO APLICADA AQUI ---
                            // Lendo a data como String e convertendo para Date
                            val dateString = doc.getString("date")
                            val date = if (dateString != null) {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                            } else {
                                // Fallback para usar o ID do documento se o campo 'date' não existir
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(id)
                            }

                            @Suppress("UNCHECKED_CAST")
                            val history = (doc.get("history") as? List<Long> ?: emptyList()).map { it.toInt() }

                            WaterIntakeEntry(
                                id = id,
                                goal = goal,
                                current = current,
                                cupSize = cupSize,
                                history = history,
                                date = date
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    trySend(Result.success(entries))
                }
            }

        awaitClose { listener.remove() }
    }
}