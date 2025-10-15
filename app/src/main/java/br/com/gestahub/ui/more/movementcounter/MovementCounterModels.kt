package br.com.gestahub.ui.more.movementcounter

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class KickSession(
    val id: String = "",
    // --- MUDANÇA 1 ---
    // Aceita qualquer tipo de objeto no campo timestamp.
    val timestamp: Any? = null,
    val kicks: Int = 0,
    @get:PropertyName("duration") @set:PropertyName("duration")
    var durationInMillis: Long = 0,
    // O campo 'date' que você mencionou não é usado para o timestamp,
    // mas o adicionamos com @get:Exclude para que o Firestore não dê erro
    // ao tentar mapeá-lo se ele existir em alguns documentos.
    @get:Exclude
    val date: String? = null
) {
    // --- MUDANÇA 2 ---
    // Helper inteligente para sempre nos dar o timestamp como um número (Long).
    @get:Exclude
    val timestampAsLong: Long
        get() = when (timestamp) {
            is Timestamp -> timestamp.toDate().time // Se for Timestamp, converte
            is Long -> timestamp // Se já for Long, usa direto
            else -> 0L // Caso seja nulo ou outro tipo, retorna 0
        }

    // O resto do código agora usa o helper e funcionará sempre.
    val dateFormatted: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestampAsLong))

    val timeFormatted: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestampAsLong))

    val durationFormatted: String
        get() {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis)
            return "$minutes min"
        }
}