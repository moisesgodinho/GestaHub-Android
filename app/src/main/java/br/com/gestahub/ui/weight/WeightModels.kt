package br.com.gestahub.ui.weight

import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Representa um único registro de peso no banco de dados.
 */
data class WeightEntry(
    @DocumentId val id: String = "",
    val date: Date = Date(),
    val weight: Double = 0.0
)