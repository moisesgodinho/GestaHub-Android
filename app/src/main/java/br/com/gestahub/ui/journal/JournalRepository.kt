// Local: app/src/main/java/br/com/gestahub/data/JournalRepository.kt
package br.com.gestahub.data

import br.com.gestahub.ui.journal.JournalEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class JournalRepository {
    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    fun getJournalEntriesListener(onUpdate: (List<JournalEntry>) -> Unit): ListenerRegistration? {
        if (userId == null) return null
        return db.collection("users").document(userId)
            .collection("symptomEntries")
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val entries = snapshot.toObjects(JournalEntry::class.java)
                onUpdate(entries)
            }
    }

    suspend fun saveJournalEntry(entry: JournalEntry) {
        if (userId == null) return
        val docRef = db.collection("users").document(userId)
            .collection("symptomEntries").document(entry.date)
        docRef.set(entry).await()
    }

    suspend fun getJournalEntry(date: String): JournalEntry? {
        if (userId == null) return null
        return try {
            val doc = db.collection("users").document(userId)
                .collection("symptomEntries").document(date).get().await()
            doc.toObject(JournalEntry::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // --- NOVA FUNÇÃO DE EXCLUSÃO ADICIONADA AQUI ---
    suspend fun deleteJournalEntry(entry: JournalEntry) {
        if (userId == null) return
        db.collection("users").document(userId)
            .collection("symptomEntries").document(entry.date).delete().await()
    }
}