package br.com.gestahub.ui.hydration

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Representa um registro diário de consumo de água
data class WaterIntakeEntry(
    val id: String = "", // O ID será a data no formato "yyyy-MM-dd"
    val goal: Int = 2500,
    val current: Int = 0,
    val cupSize: Int = 250,
    val history: List<Int> = emptyList(),
    @ServerTimestamp
    val date: Date? = null
)