package br.com.gestahub.ui.medicationtracker

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Medication(
    @DocumentId val id: String = "",
    val name: String = "",
    val dosage: String? = null,
    val notes: String? = null,
    val scheduleType: String = "FIXED_TIMES",
    val doses: List<String> = emptyList(),
    // V CORREÇÃO AQUI: Trocado Int? para Long? V
    val intervalHours: Long? = null,
    val durationType: String = "CONTINUOUS",
    val startDate: String = "",
    val durationValue: Long? = null,
    // ^ CORREÇÃO AQUI: Trocado Int? para Long? ^
    val createdAt: Timestamp = Timestamp.now()
)

data class MedicationHistoryDocument(
    @DocumentId val date: String = "",
    val data: Map<String, List<Long>> = emptyMap() // Firestore usa Long para listas de números
)

typealias MedicationHistoryMap = Map<String, Map<String, List<Int>>>