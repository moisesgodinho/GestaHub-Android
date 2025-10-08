// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalCalendar.kt
package br.com.gestahub.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun JournalCalendar(
    entries: List<JournalEntry>,
    displayMonth: YearMonth
) {
    val firstDayOfMonth = displayMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = displayMonth.lengthOfMonth()
    val today = LocalDate.now()

    val cardColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("D", "S", "T", "Q", "Q", "S", "S")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var dayCounter = 1
            for (week in 0 until 6) {
                if (dayCounter > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 0 until 7) {
                        if (week == 0 && day < firstDayOfWeek) {
                            Box(modifier = Modifier.weight(1f))
                        } else if (dayCounter <= daysInMonth) {
                            val date = displayMonth.atDay(dayCounter)
                            val entryForDay = entries.find { LocalDate.parse(it.date) == date }
                            DayCell(
                                day = dayCounter,
                                isToday = date == today,
                                entry = entryForDay
                            )
                            dayCounter++
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCell(
    day: Int,
    isToday: Boolean,
    entry: JournalEntry?
) {
    val moodsMap = mapOf(
        "Feliz" to "😄", "Tranquila" to "😌", "Amorosa" to "🥰", "Animada" to "🎉",
        "Cansada" to "😴", "Sonolenta" to "🥱", "Sensível" to "🥺", "Ansiosa" to "😟",
        "Preocupada" to "🤔", "Irritada" to "😠", "Indisposta" to "🤢", "Com dores" to "😖"
    )

    val contentColor = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            // --- ESTILO ALTERADO AQUI ---
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (entry != null) {
            val moodEmoji = moodsMap[entry.mood]
            if (moodEmoji != null) {
                Text(text = moodEmoji, fontSize = 20.sp)
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp))
                )
            }
        } else {
            Text(
                text = day.toString(),
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}