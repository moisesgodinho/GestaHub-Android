package br.com.gestahub.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun JournalCalendar(
    entries: List<JournalEntry>,
    displayMonth: YearMonth,
    minDate: LocalDate?,
    onDateClick: (date: LocalDate, entry: JournalEntry?) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean,
    isDarkTheme: Boolean
) {
    val firstDayOfMonth = displayMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = displayMonth.lengthOfMonth()
    val today = LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MonthNavigator(
                selectedMonth = displayMonth,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                isPreviousEnabled = isPreviousEnabled,
                isNextEnabled = isNextEnabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("D", "S", "T", "Q", "Q", "S", "S")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
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
                            val isDateValid = !date.isAfter(today) && (minDate == null || !date.isBefore(minDate))

                            DayCell(
                                day = dayCounter,
                                isToday = date == today,
                                entry = entryForDay,
                                isEnabled = isDateValid,
                                onClick = { if (isDateValid) onDateClick(date, entryForDay) }
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
private fun MonthNavigator(
    selectedMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousClick, enabled = isPreviousEnabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
        }
        Text(
            text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} de ${selectedMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextClick, enabled = isNextEnabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
        }
    }
}


@Composable
fun RowScope.DayCell(
    day: Int,
    isToday: Boolean,
    entry: JournalEntry?,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    // --- CORREÇÃO APLICADA AQUI ---
    // Agora o mapa usa a chave de texto correta (Ex: "Com dores")
    // para encontrar o emoji (Ex: "😖")
    val moodsMap = mapOf(
        "Feliz" to "😄",
        "Tranquila" to "😌",
        "Amorosa" to "🥰",
        "Animada" to "🎉",
        "Cansada" to "😴",
        "Sonolenta" to "🥱",
        "Sensível" to "🥺",
        "Ansiosa" to "😟",
        "Preocupada" to "🤔",
        "Irritada" to "😠",
        "Indisposta" to "🤢",
        "Com dores" to "😖"
    )

    var contentColor = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    if (!isEnabled) {
        contentColor = contentColor.copy(alpha = 0.38f)
    }
    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            color = contentColor,
            style = MaterialTheme.typography.bodySmall
        )

        if (entry != null) {
            val moodEmoji = moodsMap[entry.mood]
            if (moodEmoji != null) {
                Text(
                    text = moodEmoji,
                    fontSize = 18.sp,
                    modifier = Modifier.alpha(if (isEnabled) 1f else 0.38f)
                )
            } else if (entry.mood.isNotBlank()){ // Adicionado para garantir que o ponto só apareça se houver um humor
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .alpha(if (isEnabled) 1f else 0.38f)
                )
            }
        }
    }
}