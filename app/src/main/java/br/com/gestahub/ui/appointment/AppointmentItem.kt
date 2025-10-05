// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentItem.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AppointmentItem(
    appointment: Appointment,
    onToggleDone: (Appointment) -> Unit,
    onEdit: (Appointment) -> Unit
) {
    // --- CORREÇÃO APLICADA AQUI ---
    val cardColors = if (appointment.done) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    } else if (appointment.type == AppointmentType.ULTRASOUND && (appointment as UltrasoundAppointment).isScheduled) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(appointment) },
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEdit(appointment) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    Checkbox(
                        // --- CORREÇÃO APLICADA AQUI ---
                        checked = appointment.done,
                        onCheckedChange = { onToggleDone(appointment) }
                    )
                }
            }

            if (!appointment.date.isNullOrBlank()) {
                val formattedDate = LocalDate.parse(appointment.date).format(
                    DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}