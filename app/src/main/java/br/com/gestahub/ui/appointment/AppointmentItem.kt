// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentItem.kt
package br.com.gestahub.ui.appointment

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import br.com.gestahub.util.GestationalAgeCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val NOTES_TRUNCATE_LENGTH = 100

@Composable
fun AppointmentItem(
    appointment: Appointment,
    lmpDate: LocalDate?,
    onToggleDone: (Appointment) -> Unit,
    onEdit: (Appointment) -> Unit,
    onDelete: (Appointment) -> Unit
) {
    val isDone = appointment.done
    val isUltrasound = appointment.type == AppointmentType.ULTRASOUND
    val isScheduled = appointment.date != null

    val borderColor = when {
        isDone -> MaterialTheme.colorScheme.secondary
        isUltrasound -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    val contentAlpha = if (isDone) 0.6f else 1f

    var isNotesExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onEdit(appointment) }
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
                    )
                )

                Spacer(Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- LÓGICA DE DATAS ATUALIZADA ---
                    // 1. Mostra a data agendada, se houver.
                    if (isScheduled) {
                        val formattedDate = LocalDate.parse(appointment.date).format(
                            DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", Locale("pt", "BR"))
                        )
                        InfoRow(icon = Icons.Default.CalendarMonth, text = "Agendado para: $formattedDate")
                    }

                    // 2. Para ultrassons, mostra SEMPRE a janela ideal.
                    if (appointment is UltrasoundAppointment) {
                        val windowText = if (lmpDate != null) {
                            val startDate = GestationalAgeCalculator.getWindowStartDate(lmpDate, appointment.startWeek)
                            val endDate = GestationalAgeCalculator.getWindowEndDate(lmpDate, appointment.endWeek)
                            "$startDate a $endDate"
                        } else {
                            // Fallback se a DUM não estiver disponível
                            "Entre ${appointment.startWeek} e ${appointment.endWeek} semanas"
                        }
                        InfoRow(
                            icon = Icons.Default.DateRange, // Ícone diferente para clareza
                            text = "Janela ideal: $windowText",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    appointment.time?.takeIf { it.isNotBlank() }?.let {
                        InfoRow(icon = Icons.Default.WatchLater, text = it)
                    }
                    // --- FIM DA LÓGICA DE DATAS ---


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
                    IconButton(
                        onClick = { onEdit(appointment) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    if (appointment is ManualAppointment) {
                        IconButton(
                            onClick = { onDelete(appointment) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Deletar")
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
            modifier = Modifier.size(18.dp).padding(top = 2.dp),
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
            modifier = Modifier.size(18.dp).padding(top = 2.dp),
            tint = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}