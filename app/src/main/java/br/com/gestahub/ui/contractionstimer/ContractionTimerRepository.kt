package br.com.gestahub.ui.contractionstimer

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContractionTimerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getContractionsCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("contractions")
    }

    fun getContractions(): Flow<List<Contraction>> = callbackFlow {
        val collection = getContractionsCollection()
        if (collection == null) {
            channel.close()
            return@callbackFlow
        }

        val listener = collection
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    trySend(it.toObjects(Contraction::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addContraction(contraction: Contraction) {
        getContractionsCollection()?.add(contraction)?.await()
    }

    suspend fun deleteContraction(contractionId: String) {
        getContractionsCollection()?.document(contractionId)?.delete()?.await()
    }
}