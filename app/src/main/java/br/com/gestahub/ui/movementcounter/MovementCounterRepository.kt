// app/src/main/java/br/com/gestahub/ui/movementcounter/MovementCounterRepository.kt

package br.com.gestahub.ui.movementcounter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await // <-- IMPORTAÇÃO QUE FALTAVA

class MovementCounterRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getKickSessions(): Flow<List<KickSession>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            awaitClose()
            return@callbackFlow
        }

        val sessionsRef = firestore.collection("users").document(userId).collection("kickSessions")
        val query = sessionsRef.orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreError", "Listen failed.", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                try {
                    val sessions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(KickSession::class.java)?.copy(id = doc.id)
                    }
                    trySend(sessions)
                } catch (e: Exception) {
                    Log.e("FirestoreError", "Error converting documents", e)
                    close(e)
                }
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addKickSession(session: KickSession) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("kickSessions")
                .add(session)
                .await() // Agora esta linha vai funcionar
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error adding kick session", e)
        }
    }

    fun deleteKickSession(sessionId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("kickSessions").document(sessionId)
            .delete()
    }
}