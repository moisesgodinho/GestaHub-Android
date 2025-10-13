// Local: app/src/main/java/br/com/gestahub/ui/appointment/components/AppointmentItem.kt
package br.com.gestahub.ui.appointment.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.appointment.Appointment
import br.com.gestahub.ui.appointment.AppointmentType
import br.com.gestahub.ui.appointment.ManualAppointment
import br.com.gestahub.ui.appointment.UltrasoundAppointment
import br.com.gestahub.ui.theme.Rose500
import br.com.gestahub.util.GestationalAgeCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val NOTES_TRUNCATE_LENGTH = 100

@Composable
fun AppointmentItem(
    appointment: Appointment,
    lmpDate: LocalDate?,
    isDarkTheme: Boolean,
    onToggleDone: (Appointment) -> Unit,
    onEdit: (Appointment) -> Unit,
    onDelete: (Appointment) -> Unit
) {
    val isDone = appointment.done
    val isUltrasound = appointment.type == AppointmentType.ULTRASOUND
    val isScheduled = appointment.date != null

    val borderColor = when {
        isDone -> MaterialTheme.colorScheme.secondary
        isUltrasound -> Rose500
        else -> MaterialTheme.colorScheme.primary
    }

    var isNotesExpanded by remember { mutableStateOf(false) }

    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )

            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggleDone(appointment) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.secondary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.scale(1.2f)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = appointment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isScheduled) {
                        val formattedDate = LocalDate.parse(appointment.date).format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
                        )
                        InfoRow(icon = Icons.Default.CalendarMonth, text = formattedDate)
                    }

                    if (appointment is UltrasoundAppointment) {
                        val windowText = if (lmpDate != null) {
                            val startDate = GestationalAgeCalculator.getWindowStartDate(lmpDate, appointment.startWeek)
                            val endDate = GestationalAgeCalculator.getWindowEndDate(lmpDate, appointment.endWeek)
                            "$startDate a $endDate"
                        } else {
                            "Entre ${appointment.startWeek} e ${appointment.endWeek} semanas"
                        }
                        InfoRow(
                            icon = Icons.Filled.DateRange,
                            text = "Janela ideal: $windowText",
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 2
                        )
                    }

                    appointment.time?.takeIf { it.isNotBlank() }?.let {
                        InfoRow(icon = Icons.Default.WatchLater, text = it)
                    }

                    val professional = (appointment as? ManualAppointment)?.professional ?: (appointment as? UltrasoundAppointment)?.professional
                    professional?.takeIf { it.isNotBlank() }?.let {
                        InfoRow(icon = Icons.Default.Person, text = it)
                    }

                    val location = (appointment as? ManualAppointment)?.location ?: (appointment as? UltrasoundAppointment)?.location
                    location?.takeIf { it.isNotBlank() }?.let {
                        InfoRow(icon = Icons.Default.LocationOn, text = it)
                    }

                    val notes = (appointment as? ManualAppointment)?.notes ?: (appointment as? UltrasoundAppointment)?.notes
                    if (!notes.isNullOrBlank()) {
                        NotesWithExpandableLink(
                            notes = notes,
                            isExpanded = isNotesExpanded,
                            onToggle = { isNotesExpanded = !isNotesExpanded }
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Row {
                    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant

                    IconButton(
                        onClick = { onEdit(appointment) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = iconColor)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    if (appointment is ManualAppointment || (appointment is UltrasoundAppointment && appointment.isScheduled)) {
                        IconButton(
                            onClick = { onDelete(appointment) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = iconColor)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Deletar ou Limpar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesWithExpandableLink(
    notes: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val isLongNote = notes.length > NOTES_TRUNCATE_LENGTH
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp),
            tint = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))

        val annotatedString = buildAnnotatedString {
            if (isExpanded || !isLongNote) {
                append(notes)
                if (isLongNote) {
                    append(" ")
                    pushStringAnnotation(tag = "expand_toggle", annotation = "toggle")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("Ver menos")
                    }
                    pop()
                }
            } else {
                append(notes.take(NOTES_TRUNCATE_LENGTH))
                append("... ")
                pushStringAnnotation(tag = "expand_toggle", annotation = "toggle")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Ver mais")
                }
                pop()
            }
        }

        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "expand_toggle", start = offset, end = offset)
                    .firstOrNull()?.let {
                        onToggle()
                    }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    color: Color? = null,
    maxLines: Int = 1
) {
    val textColor = color ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp),
            tint = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            maxLines = maxLines,
            overflow = if (maxLines > 1) TextOverflow.Clip else TextOverflow.Ellipsis
        )
    }
}