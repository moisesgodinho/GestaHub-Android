package br.com.gestahub.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GestationalProfileRepository {
    private val db = Firebase.firestore
    // O userId foi removido daqui para não guardar um valor antigo.

    // Agora a função recebe o userId que deve ser usado.
    fun getGestationalProfileFlow(userId: String) = db.collection("users").document(userId)

    suspend fun saveLmp(lmp: String) {
        // Nas funções de salvar, pegamos o usuário atual no momento da chamada.
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) return
        val userDocRef = db.collection("users").document(userId)
        val gestationalProfile = mapOf("lmp" to lmp)
        userDocRef.set(mapOf("gestationalProfile" to gestationalProfile), SetOptions.merge()).await()
    }

    suspend fun saveUltrasound(examDate: String, weeks: String, days: String) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) return
        val userDocRef = db.collection("users").document(userId)
        val ultrasoundData = mapOf(
            "examDate" to examDate,
            "weeksAtExam" to weeks,
            "daysAtExam" to days
        )
        val gestationalProfile = mapOf("ultrasound" to ultrasoundData)
        userDocRef.set(mapOf("gestationalProfile" to gestationalProfile), SetOptions.merge()).await()
    }
}