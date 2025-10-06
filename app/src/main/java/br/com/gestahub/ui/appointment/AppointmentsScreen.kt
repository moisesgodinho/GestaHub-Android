// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentsScreen.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val INITIAL_VISIBLE_PAST_APPOINTMENTS = 5
private const val LOAD_MORE_COUNT = 5

@Composable
fun AppointmentsScreen(
    contentPadding: PaddingValues,
    uiState: AppointmentsUiState,
    isDarkTheme: Boolean,
    onToggleDone: (Appointment) -> Unit,
    onEditClick: (Appointment) -> Unit,
    onDeleteOrClearRequest: (Appointment) -> Unit,
    onNavigateToForm: (date: String?) -> Unit
) {
    var visiblePastCount by remember { mutableStateOf(INITIAL_VISIBLE_PAST_APPOINTMENTS) }

    // Estados para controlar os diálogos
    var showNewAppointmentDialogForDate by remember { mutableStateOf<LocalDate?>(null) }
    var appointmentsToShowInDialog by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var dialogDate by remember { mutableStateOf<LocalDate?>(null) }

    // Diálogo para dias vazios
    showNewAppointmentDialogForDate?.let { date ->
        NewAppointmentDialog(
            date = date,
            onDismiss = { showNewAppointmentDialogForDate = null },
            onConfirm = {
                onNavigateToForm(it.format(DateTimeFormatter.ISO_LOCAL_DATE))
                showNewAppointmentDialogForDate = null
            }
        )
    }

    // Diálogo para dias com consultas
    if (appointmentsToShowInDialog.isNotEmpty()) {
        dialogDate?.let { date ->
            ViewAppointmentsDialog(
                date = date,
                appointments = appointmentsToShowInDialog,
                onDismiss = { appointmentsToShowInDialog = emptyList() },
                onEdit = {
                    onEditClick(it)
                    appointmentsToShowInDialog = emptyList()
                },
                onDelete = {
                    onDeleteOrClearRequest(it)
                    // Atualiza a lista do diálogo em tempo real ao deletar
                    appointmentsToShowInDialog = appointmentsToShowInDialog.filterNot { item -> item.id == it.id }
                },
                onAddNew = {
                    onNavigateToForm(it.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    appointmentsToShowInDialog = emptyList()
                }
            )
        }
    }


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
                isDarkTheme = isDarkTheme,
                onDateClick = { date, appointmentsOnDay ->
                    if (appointmentsOnDay.isNotEmpty()) {
                        dialogDate = date
                        appointmentsToShowInDialog = appointmentsOnDay.sortedBy { it.time }
                    } else {
                        showNewAppointmentDialogForDate = date
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
                                    isDarkTheme = isDarkTheme,
                                    onToggleDone = onToggleDone,
                                    onEdit = onEditClick,
                                    onDelete = onDeleteOrClearRequest
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
                                    isDarkTheme = isDarkTheme,
                                    onToggleDone = onToggleDone,
                                    onEdit = onEditClick,
                                    onDelete = onDeleteOrClearRequest
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