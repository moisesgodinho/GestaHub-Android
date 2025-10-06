// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentCalendar.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width // <<< --- IMPORTAÇÃO ADICIONADA AQUI ---
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onDateClick: (LocalDate, List<Appointment>) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    val appointmentsByDate = remember(appointments) {
        appointments.filter { it.date != null }
            .groupBy { LocalDate.parse(it.date) }
    }

    val windowColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    )

    val ultrasoundWindows = remember(lmpDate, windowColors) {
        if (lmpDate == null) return@remember emptyMap()
        val windows = mutableMapOf<LocalDate, Color>()

        AppointmentData.ultrasoundSchedule.forEachIndexed { index, exam ->
            val startDate = lmpDate.plusWeeks(exam.startWeek.toLong())
            val endDate = lmpDate.plusWeeks(exam.endWeek.toLong()).plusDays(6)
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
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CalendarHeader(
                yearMonth = currentMonth,
                onPrevMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            CalendarGrid(
                yearMonth = currentMonth,
                today = today,
                appointmentsByDate = appointmentsByDate,
                ultrasoundWindows = ultrasoundWindows,
                onDateClick = onDateClick
            )
            if (lmpDate != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CalendarLegend(windowColors = windowColors)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarLegend(windowColors: List<Color>) {
    Column {
        Divider(modifier = Modifier.padding(bottom = 12.dp))
        Text(
            text = "Legenda das Janelas Ideais:",
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
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                label = "Consultas/Exames Agendados"
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
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês Anterior")
        }
        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${yearMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
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
    onClick: () -> Unit
) {
    val hasAppointments = appointments.isNotEmpty()
    var backgroundColor = windowColor ?: Color.Transparent
    if (hasAppointments) {
        backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
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
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (hasAppointments) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    appointments.take(3).forEach { app ->
                        val color = if (app.type == AppointmentType.ULTRASOUND) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        Box(modifier = Modifier.size(4.dp).background(color, CircleShape))
                    }
                }
            }
        }
    }
}