// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentItem.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AppointmentItem(
    appointment: Appointment,
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(contentAlpha),
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

                    if (isScheduled) {
                        val formattedDate = LocalDate.parse(appointment.date).format(
                            DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", Locale("pt", "BR"))
                        )
                        InfoRow(icon = Icons.Default.CalendarMonth, text = formattedDate)
                    } else if (appointment is UltrasoundAppointment) {
                        InfoRow(
                            icon = Icons.Default.CalendarMonth,
                            text = "Janela ideal: ${appointment.startWeek} a ${appointment.endWeek} semanas",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    appointment.time?.takeIf { it.isNotBlank() }?.let {
                        InfoRow(icon = Icons.Default.WatchLater, text = it)
                    }

                    // --- INÍCIO DAS NOVAS INFORMAÇÕES ---
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
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // --- FIM DAS NOVAS INFORMAÇÕES ---
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
private fun InfoRow(icon: ImageVector, text: String, color: Color? = null) {
    val textColor = color ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}