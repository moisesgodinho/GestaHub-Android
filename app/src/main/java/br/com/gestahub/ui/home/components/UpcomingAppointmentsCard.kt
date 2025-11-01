package br.com.gestahub.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import br.com.gestahub.ui.appointment.Appointment
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun UpcomingAppointmentsCard(
    appointments: List<Appointment>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Próximos Compromissos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                // --- CORREÇÃO DE NAVEGAÇÃO APLICADA AQUI ---
                TextButton(onClick = {
                    navController.navigate("appointments") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Text("Ver todos")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (appointments.isEmpty()) {
                Text(
                    "Nenhum compromisso agendado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    appointments.forEach { appointment ->
                        AppointmentRow(appointment = appointment)
                        if (appointment != appointments.last()) {
                            Divider(modifier = Modifier.padding(start = 56.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentRow(appointment: Appointment) {
    val date = appointment.date?.let { LocalDate.parse(it) } ?: return
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }
    val dayOfMonth = date.dayOfMonth
    val time = appointment.time?.let { "às $it" } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(56.dp)
        ) {
            Text(
                dayOfWeek,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                appointment.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (time.isNotEmpty()) {
                Text(
                    time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ver detalhes",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}