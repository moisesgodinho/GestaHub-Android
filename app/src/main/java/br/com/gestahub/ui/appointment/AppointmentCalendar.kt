// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentCalendar.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppointmentCalendar(
    appointments: List<Appointment>,
    lmpDate: LocalDate?,
    isDarkTheme: Boolean,
    onDateClick: (LocalDate, List<Appointment>) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    val appointmentsByDate = remember(appointments) {
        appointments.filter { it.date != null }
            .groupBy { LocalDate.parse(it.date) }
    }

    val windowColors = if (isDarkTheme) {
        // Cores para o Modo Escuro
        listOf(
            Color(0xFF312E81), // Indigo 900
            Color(0xFF065F46), // Green 900
            Color(0xFF78350F), // Amber 900
            Color(0xFF581C87), // Purple 900
            Color(0xFF155E75)  // Cyan 900
        )
    } else {
        // Cores para o Modo Claro
        listOf(
            Color(0xFFE0E7FF), // Indigo 100
            Color(0xFFD1FAE5), // Green 100
            Color(0xFFFEF3C7), // Amber 100
            Color(0xFFF3E8FF), // Purple 100
            Color(0xFFCCFBF1)  // Teal 100
        )
    }

    val scheduledColor = if(isDarkTheme) Color(0xFF7F1D1D) else Color(0xFFFECACA)

    val ultrasoundWindows = remember(lmpDate, windowColors) {
        if (lmpDate == null) return@remember emptyMap()
        val windows = mutableMapOf<LocalDate, Color>()

        AppointmentData.ultrasoundSchedule.forEachIndexed { index, exam ->
            val startDate = lmpDate.plusWeeks((exam.startWeek - 1).toLong())
            val endDate = lmpDate.plusWeeks(exam.endWeek.toLong()).minusDays(1)
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                windows[currentDate] = windowColors[index % windowColors.size]
                currentDate = currentDate.plusDays(1)
            }
        }
        windows
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CalendarHeader(
                yearMonth = currentMonth,
                onPrevMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                isPrevEnabled = lmpDate?.let { YearMonth.from(it.minusMonths(2)).isBefore(currentMonth) } ?: true,
                isNextEnabled = lmpDate?.let { YearMonth.from(it.plusDays(280).plusMonths(2)).isAfter(currentMonth) } ?: true
            )
            Spacer(modifier = Modifier.height(16.dp))
            CalendarGrid(
                yearMonth = currentMonth,
                today = today,
                appointmentsByDate = appointmentsByDate,
                ultrasoundWindows = ultrasoundWindows,
                scheduledColor = scheduledColor,
                isDarkTheme = isDarkTheme,
                onDateClick = onDateClick
            )
            if (lmpDate != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CalendarLegend(windowColors = windowColors, scheduledColor = scheduledColor)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarLegend(windowColors: List<Color>, scheduledColor: Color) {
    Column {
        Divider(modifier = Modifier.padding(bottom = 12.dp))
        Text(
            text = "Legenda:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegendItem(
                color = scheduledColor,
                label = "Dia com Agendamento"
            )
            AppointmentData.ultrasoundSchedule.forEachIndexed { index, ultrasound ->
                LegendItem(
                    color = windowColors[index % windowColors.size],
                    label = ultrasound.name
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun CalendarHeader(
    yearMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    isPrevEnabled: Boolean,
    isNextEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevMonth, enabled = isPrevEnabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês Anterior")
        }
        Text(
            // --- ALTERAÇÃO APLICADA AQUI ---
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} de ${yearMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth, enabled = isNextEnabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo Mês")
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    appointmentsByDate: Map<LocalDate, List<Appointment>>,
    ultrasoundWindows: Map<LocalDate, Color>,
    scheduledColor: Color,
    isDarkTheme: Boolean,
    onDateClick: (LocalDate, List<Appointment>) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7
    val daysOfWeek = listOf("D", "S", "T", "Q", "Q", "S", "S")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
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

        val totalCells = (daysInMonth + firstDayOfMonth + 6) / 7 * 7
        for (week in 0 until totalCells / 7) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    val cellIndex = week * 7 + dayOfWeek
                    val dayOfMonth = cellIndex - firstDayOfMonth + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayOfMonth)
                        val appointmentsForDay = appointmentsByDate[date] ?: emptyList()
                        DayCell(
                            date = date,
                            isToday = date.isEqual(today),
                            appointments = appointmentsForDay,
                            windowColor = ultrasoundWindows[date],
                            scheduledColor = scheduledColor,
                            isDarkTheme = isDarkTheme,
                            onClick = { onDateClick(date, appointmentsForDay) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    date: LocalDate,
    isToday: Boolean,
    appointments: List<Appointment>,
    windowColor: Color?,
    scheduledColor: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val hasAppointments = appointments.isNotEmpty()

    val backgroundColor = when {
        hasAppointments -> scheduledColor
        windowColor != null -> windowColor
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val textColor = when {
        isDarkTheme && (hasAppointments || windowColor != null) -> Color.White.copy(alpha = 0.9f)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (hasAppointments) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    appointments.take(3).forEach { app ->
                        val dotColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                        Box(modifier = Modifier.size(4.dp).background(dotColor, CircleShape))
                    }
                }
            }
        }
    }
}