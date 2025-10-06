// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentsScreen.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private const val INITIAL_VISIBLE_PAST_APPOINTMENTS = 5
private const val LOAD_MORE_COUNT = 5

@Composable
fun AppointmentsScreen(
    contentPadding: PaddingValues,
    uiState: AppointmentsUiState,
    onToggleDone: (Appointment) -> Unit,
    onEditClick: (Appointment) -> Unit,
    onDeleteRequest: (Appointment) -> Unit,
    onNavigateToForm: (date: String?) -> Unit // Alterado para aceitar uma data
) {
    var visiblePastCount by remember { mutableStateOf(INITIAL_VISIBLE_PAST_APPOINTMENTS) }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            AppointmentCalendar(
                appointments = uiState.upcomingAppointments + uiState.pastAppointments,
                lmpDate = uiState.lmpDate,
                onDateClick = { date, appointmentsOnDay ->
                    if (appointmentsOnDay.isNotEmpty()) {
                        // Se houver consultas, edita a primeira da lista
                        onEditClick(appointmentsOnDay.first())
                    } else {
                        // Se o dia estiver vazio, navega para o formulário com a data
                        onNavigateToForm(date.toString())
                    }
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                // ... (o restante do conteúdo do Card permanece o mesmo)
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.upcomingAppointments.isEmpty() && uiState.pastAppointments.isEmpty()) {
                        Text(
                            text = "Nenhuma consulta encontrada. Toque no '+' para adicionar a primeira.",
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        if (uiState.upcomingAppointments.isNotEmpty()) {
                            SectionHeader("Próximas Consultas")
                            uiState.upcomingAppointments.forEach { appointment ->
                                AppointmentItem(
                                    appointment = appointment,
                                    lmpDate = uiState.lmpDate,
                                    onToggleDone = onToggleDone,
                                    onEdit = onEditClick,
                                    onDelete = onDeleteRequest
                                )
                            }
                        }

                        if (uiState.pastAppointments.isNotEmpty()) {
                            if (uiState.upcomingAppointments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            SectionHeader("Consultas Passadas")

                            val pastAppointmentsToShow = uiState.pastAppointments.take(visiblePastCount)

                            pastAppointmentsToShow.forEach { appointment ->
                                AppointmentItem(
                                    appointment = appointment,
                                    lmpDate = uiState.lmpDate,
                                    onToggleDone = onToggleDone,
                                    onEdit = onEditClick,
                                    onDelete = onDeleteRequest
                                )
                            }

                            if (uiState.pastAppointments.size > visiblePastCount) {
                                OutlinedButton(
                                    onClick = { visiblePastCount += LOAD_MORE_COUNT },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mostrar mais")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}