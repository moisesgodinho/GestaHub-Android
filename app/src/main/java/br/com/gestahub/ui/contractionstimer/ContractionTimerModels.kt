package br.com.gestahub.ui.contractionstimer

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Contraction(
    @DocumentId val id: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val duration: Long = 0L, // in seconds
    val frequency: Long = 0L // in seconds
)