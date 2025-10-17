package br.com.gestahub.ui.hydration

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HydrationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getWaterIntakeCollection() =
        auth.currentUser?.uid?.let { db.collection("users").document(it).collection("waterIntake") }

    fun getWaterHistory(): Flow<Result<List<WaterIntakeEntry>>> = callbackFlow {
        // --- CORREÇÃO APLICADA AQUI ---
        getWaterIntakeCollection()?.let { collection ->
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
                                val dateString = doc.getString("date") ?: id
                                @Suppress("UNCHECKED_CAST")
                                val history = (doc.get("history") as? List<Long> ?: emptyList()).map { it.toInt() }
                                WaterIntakeEntry(id, goal, current, cupSize, history, dateString)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                        trySend(Result.success(entries))
                    }
                }
            awaitClose { listener.remove() }
        } ?: trySend(Result.failure(Exception("Usuário não autenticado.")))
    }

    fun listenToTodayWaterIntake(): Flow<Result<WaterIntakeEntry?>> = callbackFlow {
        // --- CORREÇÃO APLICADA AQUI ---
        getWaterIntakeCollection()?.let { collection ->
            val todayId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val docRef = collection.document(todayId)

            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val id = snapshot.id
                        val goal = (snapshot.getLong("goal") ?: 2500L).toInt()
                        val current = (snapshot.getLong("current") ?: 0L).toInt()
                        val cupSize = (snapshot.getLong("cupSize") ?: 250L).toInt()
                        val dateString = snapshot.getString("date") ?: id
                        @Suppress("UNCHECKED_CAST")
                        val history = (snapshot.get("history") as? List<Long> ?: emptyList()).map { it.toInt() }
                        val entry = WaterIntakeEntry(id, goal, current, cupSize, history, dateString)
                        trySend(Result.success(entry))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                } else {
                    trySend(Result.success(null))
                }
            }
            awaitClose { listener.remove() }
        } ?: trySend(Result.failure(Exception("Usuário não autenticado.")))
    }

    suspend fun updateWaterData(entry: WaterIntakeEntry) {
        // --- CORREÇÃO APLICADA AQUI ---
        getWaterIntakeCollection()?.let { collection ->
            collection.document(entry.id).set(entry, SetOptions.merge()).await()
        }
    }
}