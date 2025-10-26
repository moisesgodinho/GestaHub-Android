package br.com.gestahub.ui.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.gestahub.ui.appointment.components.AppointmentsListCard
import br.com.gestahub.ui.components.ConfirmationDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AppointmentsScreen(
    contentPadding: PaddingValues,
    viewModel: AppointmentsViewModel = hiltViewModel(),
    isDarkTheme: Boolean,
    onNavigateToFormWithDate: (date: String?) -> Unit,
    onNavigateToFormWithAppointment: (appointment: Appointment) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- ESTADO LOCAL PARA TODOS OS DIÁLOGOS ---
    var showNewAppointmentDialogForDate by remember { mutableStateOf<LocalDate?>(null) }
    var appointmentsToShowInDialog by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var dialogDate by remember { mutableStateOf<LocalDate?>(null) }
    var itemToDeleteOrClear by remember { mutableStateOf<Appointment?>(null) }

    // --- LÓGICA UNIFICADA PARA O DIÁLOGO DE CONFIRMAÇÃO ---
    itemToDeleteOrClear?.let { appointment ->
        val isManualDelete = appointment.type == AppointmentType.MANUAL
        val title = if (isManualDelete) "Confirmar Exclusão" else "Limpar Agendamento"
        val text = if (isManualDelete) {
            "Tem certeza que deseja apagar a consulta \"${appointment.title}\"?"
        } else {
            "Tem certeza que deseja limpar os dados do agendamento para \"${appointment.title}\"? O item permanecerá na lista."
        }
        val confirmButtonText = if (isManualDelete) "Excluir" else "Limpar"

        ConfirmationDialog(
            title = title,
            text = text,
            confirmButtonText = confirmButtonText,
            onConfirm = {
                if (isManualDelete) {
                    viewModel.deleteAppointment(appointment)
                } else {
                    viewModel.clearUltrasoundSchedule(appointment)
                }
                // Se o diálogo de visualização estiver aberto, atualiza a lista dele
                appointmentsToShowInDialog = appointmentsToShowInDialog.filterNot { it.id == appointment.id }
                itemToDeleteOrClear = null // Fecha o diálogo de confirmação
            },
            onDismissRequest = { itemToDeleteOrClear = null }
        )
    }

    // Diálogo para dias sem consultas
    showNewAppointmentDialogForDate?.let { date ->
        NewAppointmentDialog(
            date = date,
            onDismiss = { showNewAppointmentDialogForDate = null },
            onConfirm = {
                onNavigateToFormWithDate(it.format(DateTimeFormatter.ISO_LOCAL_DATE))
                showNewAppointmentDialogForDate = null
            }
        )
    }

    // Diálogo para dias com consultas (modal de visualização)
    if (appointmentsToShowInDialog.isNotEmpty()) {
        dialogDate?.let { date ->
            ViewAppointmentsDialog(
                date = date,
                appointments = appointmentsToShowInDialog,
                onDismiss = { appointmentsToShowInDialog = emptyList() },
                onEdit = {
                    onNavigateToFormWithAppointment(it)
                    appointmentsToShowInDialog = emptyList()
                },
                onDelete = { appointment ->
                    // A ação de deletar agora apenas define qual item será deletado/limpo
                    itemToDeleteOrClear = appointment
                },
                onAddNew = {
                    onNavigateToFormWithDate(it.format(DateTimeFormatter.ISO_LOCAL_DATE))
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
                            onToggleDone = { viewModel.toggleDone(it) },
                            onEditClick = { onNavigateToFormWithAppointment(it) },
                            onDeleteOrClearRequest = { itemToDeleteOrClear = it } // <-- Unificado
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
                            onToggleDone = { viewModel.toggleDone(it) },
                            onEditClick = { onNavigateToFormWithAppointment(it) },
                            onDeleteOrClearRequest = { itemToDeleteOrClear = it } // <-- Unificado
                        )
                    }
                }
            }
        }
    }
}