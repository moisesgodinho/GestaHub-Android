package br.com.gestahub.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor() {
    private val db = Firebase.firestore

    // A função agora recebe o userId para montar o caminho correto
    fun getAppointmentsFlow(userId: String) =
        db.collection("users").document(userId).collection("appointments")
}