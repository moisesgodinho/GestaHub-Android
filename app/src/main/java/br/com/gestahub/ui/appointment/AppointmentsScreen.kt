// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentsScreen.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.appointment.components.AppointmentsListCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        Box(
            Modifier
                .fillMaxSize()
                .padding(contentPadding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
            }

            if (uiState.upcomingAppointments.isEmpty() && uiState.pastAppointments.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma consulta encontrada. Toque no '+' para adicionar a primeira.",
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                if (uiState.upcomingAppointments.isNotEmpty()) {
                    item {
                        AppointmentsListCard(
                            title = "Próximas Consultas",
                            appointments = uiState.upcomingAppointments,
                            lmpDate = uiState.lmpDate,
                            isDarkTheme = isDarkTheme,
                            onToggleDone = onToggleDone,
                            onEditClick = onEditClick,
                            onDeleteOrClearRequest = onDeleteOrClearRequest
                        )
                    }
                }

                if (uiState.pastAppointments.isNotEmpty()) {
                    item {
                        AppointmentsListCard(
                            title = "Consultas Passadas",
                            appointments = uiState.pastAppointments,
                            lmpDate = uiState.lmpDate,
                            isDarkTheme = isDarkTheme,
                            onToggleDone = onToggleDone,
                            onEditClick = onEditClick,
                            onDeleteOrClearRequest = onDeleteOrClearRequest
                        )
                    }
                }
            }
        }
    }
}