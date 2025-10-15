package br.com.gestahub.ui.more.movementcounter

import br.com.gestahub.util.GestationalAge
import br.com.gestahub.util.GestationalAgeCalculator
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

data class KickSession(
    val id: String = "",
    val timestamp: Any? = null,
    val kicks: Int = 0,
    // Renomeamos para 'durationInSeconds' para maior clareza
    @get:PropertyName("duration") @set:PropertyName("duration")
    var durationInSeconds: Long = 0,
    @get:Exclude
    val date: String? = null
) {
    @get:Exclude
    val timestampAsLong: Long
        get() = when (timestamp) {
            is Timestamp -> timestamp.toDate().time
            is Long -> timestamp
            else -> 0L
        }

    // --- CORREÇÃO APLICADA AQUI ---
    // Multiplicamos por 1000 para converter para milissegundos
    @get:Exclude
    private val durationInMillis: Long
        get() = durationInSeconds * 1000

    val dateFormatted: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestampAsLong))

    val startTimeFormatted: String
        get() {
            // Usamos o valor corrigido em milissegundos
            val startTime = timestampAsLong - durationInMillis
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(startTime))
        }

    val endTimeFormatted: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestampAsLong))

    val durationFormatted: String
        get() {
            if (durationInMillis < 0) return "00:00"
            // Usamos o valor corrigido em milissegundos
            val duration = Duration.ofMillis(durationInMillis)
            val minutes = duration.toMinutes()
            val seconds = duration.minusMinutes(minutes).seconds
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

    fun getGestationalAge(lmp: LocalDate?): String {
        if (lmp == null) return ""
        val sessionDate = Date(timestampAsLong).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val age = GestationalAgeCalculator.calculateGestationalAge(lmp, sessionDate)
        return "${age.weeks}s ${age.days}d"
    }
}