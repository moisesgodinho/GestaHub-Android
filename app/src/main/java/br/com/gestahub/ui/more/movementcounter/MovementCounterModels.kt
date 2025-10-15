package br.com.gestahub.ui.more.movementcounter

import br.com.gestahub.util.GestationalAgeCalculator
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class KickSession(
    val id: String = "",
    val timestamp: Any? = null,
    val kicks: Int = 0,
    @get:PropertyName("duration") @set:PropertyName("duration")
    var durationInMillis: Long = 0,
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

    // --- NOVAS PROPRIEDADES ---
    val dateFormatted: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestampAsLong))

    val startTimeFormatted: String
        get() {
            val startTime = timestampAsLong - durationInMillis
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(startTime))
        }

    val endTimeFormatted: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestampAsLong))

    val durationFormatted: String
        get() {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    fun getGestationalAge(lmp: LocalDate?): String {
        if (lmp == null) return ""
        val sessionDate = Date(timestampAsLong).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val age = GestationalAgeCalculator.calculateGestationalAge(lmp, sessionDate)
        return "${age.weeks}s ${age.days}d"
    }
}