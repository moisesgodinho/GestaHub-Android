package br.com.gestahub.ui.medicationtracker

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "MedicationRepository"

    private fun getMedsCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("medications")
    }

    private fun getHistoryCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("medicationHistory")
    }

    // V CORREÇÃO DEFINITIVA AQUI: Leitura manual e segura V
    fun getMedications(): Flow<List<Medication>> = callbackFlow {
        val collection = getMedsCollection()
        if (collection == null) {
            channel.close(); return@callbackFlow
        }

        val listener = collection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erro ao buscar medicamentos:", error)
                    close(error); return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                // Leitura manual para máxima segurança
                val medsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Tentamos a conversão automática, que é mais simples
                        doc.toObject<Medication>()
                    } catch (e: Exception) {
                        // Se falhar (ex: tipo de dado incorreto), logamos o erro e retornamos null
                        // para que este item específico seja ignorado, em vez de crashar o app.
                        Log.e(TAG, "Erro ao converter o documento ${doc.id}", e)
                        null
                    }
                }
                trySend(medsList)
            }
        awaitClose { listener.remove() }
    }

    fun getHistory(): Flow<MedicationHistoryMap> = callbackFlow {
        val collection = getHistoryCollection()
        if (collection == null) {
            channel.close(); return@callbackFlow
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Erro ao buscar histórico de medicação:", error)
                close(error); return@addSnapshotListener
            }

            if (snapshot == null) {
                trySend(emptyMap()); return@addSnapshotListener
            }

            val historyMap = mutableMapOf<String, Map<String, List<Int>>>()
            for (doc in snapshot.documents) {
                try {
                    val dataField = doc.get("data") as? Map<*, *>
                    if (dataField != null) {
                        val medMap = mutableMapOf<String, List<Int>>()
                        dataField.forEach { (key, value) ->
                            val medId = key as? String
                            val doseIndices = (value as? List<*>)?.mapNotNull { (it as? Long)?.toInt() }

                            if (medId != null && doseIndices != null) {
                                medMap[medId] = doseIndices
                            }
                        }
                        historyMap[doc.id] = medMap
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar o documento de histórico ${doc.id}", e)
                }
            }
            trySend(historyMap)
        }
        awaitClose { listener.remove() }
    }
    // ^ FIM DA CORREÇÃO ^

    suspend fun addMedication(medication: Medication) {
        getMedsCollection()?.add(medication)?.await()
    }

    suspend fun updateMedication(medId: String, medication: Medication) {
        getMedsCollection()?.document(medId)?.set(medication)?.await()
    }

    suspend fun deleteMedication(medId: String) {
        getMedsCollection()?.document(medId)?.delete()?.await()
    }

    suspend fun toggleDose(medId: String, dateString: String, doseIndex: Int) {
        val historyRef = getHistoryCollection()?.document(dateString) ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        db.runTransaction { transaction ->
            val snapshot = transaction.get(historyRef)
            val dataField = snapshot.get("data.$medId") as? List<*>
            val takenDoses = dataField?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()

            if (takenDoses.contains(doseIndex)) {
                if (dateString < today) {
                    throw IllegalStateException("Não é possível desmarcar uma dose de um dia anterior.")
                }
                transaction.update(historyRef, "data.$medId", FieldValue.arrayRemove(doseIndex))
            } else {
                transaction.set(
                    historyRef,
                    mapOf("data" to mapOf(medId to FieldValue.arrayUnion(doseIndex))),
                    SetOptions.merge()
                )
            }
        }.await()
    }
}