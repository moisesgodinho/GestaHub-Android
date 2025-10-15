package br.com.gestahub.ui.more.movementcounter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MovementCounterRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getKickSessions(): Flow<List<KickSession>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList()) // Envia lista vazia se não há usuário
            awaitClose()
            return@callbackFlow
        }

        val sessionsRef = firestore.collection("users").document(userId).collection("kickSessions")
        val query = sessionsRef.orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            // --- MUDANÇA IMPORTANTE AQUI ---
            if (error != null) {
                // Logamos o erro para ver no Logcat o que aconteceu
                Log.w("FirestoreError", "Listen failed.", error)
                // Fechamos o fluxo com a exceção para que o ViewModel possa tratá-la
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Usamos um bloco try-catch para proteger contra erros de conversão de dados
                try {
                    val sessions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(KickSession::class.java)?.copy(id = doc.id)
                    }
                    trySend(sessions) // Envia os dados com sucesso
                } catch (e: Exception) {
                    Log.e("FirestoreError", "Error converting documents", e)
                    close(e) // Fecha o fluxo se a conversão falhar
                }
            }
        }

        // Isso é chamado quando o fluxo é cancelado, e remove o listener
        awaitClose { listener.remove() }
    }
}