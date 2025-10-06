// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentDialogs.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Diálogo para dias sem consultas.
 */
@Composable
fun NewAppointmentDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val formattedDate = remember(date) {
        date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("pt", "BR")))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nenhum registro encontrado") },
        text = { Text("Nenhum evento para o dia $formattedDate. Deseja adicionar uma nova consulta?") },
        confirmButton = {
            Button(onClick = { onConfirm(date) }) {
                Text("Agendar Consulta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * Diálogo unificado para dias com uma ou mais consultas.
 */
@Composable
fun ViewAppointmentsDialog(
    date: LocalDate,
    appointments: List<Appointment>,
    onDismiss: () -> Unit,
    onEdit: (Appointment) -> Unit,
    onDelete: (Appointment) -> Unit
) {
    val formattedDate = remember(date) {
        date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("pt", "BR")))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Consultas para ${formattedDate}") },
        text = {
            LazyColumn {
                items(appointments, key = { it.id + it.type.name }) { appointment ->
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            // O Arrangement foi removido para que o weight funcione corretamente
                        ) {
                            // Coluna de Texto com weight
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = appointment.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                appointment.time?.takeIf { it.isNotBlank() }?.let {
                                    Text(text = "às $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // Botões agora são filhos diretos da Row principal
                            // e serão empurrados para a direita pelo weight da Column.
                            IconButton(onClick = { onEdit(appointment) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            if (appointment.type == AppointmentType.MANUAL) {
                                IconButton(onClick = { onDelete(appointment) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val professional = (appointment as? ManualAppointment)?.professional ?: (appointment as? UltrasoundAppointment)?.professional
                        professional?.takeIf { it.isNotBlank() }?.let {
                            DialogInfoRow(icon = Icons.Default.Person, text = it)
                        }

                        val location = (appointment as? ManualAppointment)?.location ?: (appointment as? UltrasoundAppointment)?.location
                        location?.takeIf { it.isNotBlank() }?.let {
                            DialogInfoRow(icon = Icons.Default.LocationOn, text = it)
                        }

                        val notes = (appointment as? ManualAppointment)?.notes ?: (appointment as? UltrasoundAppointment)?.notes
                        notes?.takeIf { it.isNotBlank() }?.let {
                            DialogInfoRow(icon = Icons.Default.Description, text = it, maxLines = 4)
                        }
                    }
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun DialogInfoRow(icon: ImageVector, text: String, maxLines: Int = 2) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}