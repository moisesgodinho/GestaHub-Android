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

    // O gerenciador de diálogos agora lida com toda a lógica de exibição.
    AppointmentDialogsHandler(
        dialogState = uiState.dialogState,
        onDismiss = { viewModel.dismissDialog() },
        onNavigateToFormWithDate = { date ->
            viewModel.dismissDialog()
            onNavigateToFormWithDate(date)
        },
        onNavigateToFormWithAppointment = { appointment ->
            viewModel.dismissDialog()
            onNavigateToFormWithAppointment(appointment)
        },
        onConfirmDeleteOrClear = { appointment ->
            // A lógica de qual ação tomar (deletar ou limpar) é decidida e executada aqui.
            if (appointment.type == AppointmentType.MANUAL) {
                viewModel.deleteAppointment(appointment)
            } else {
                viewModel.clearUltrasoundSchedule(appointment)
            }
            viewModel.dismissDialog()
        }
    )

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
                    // A ação de clique agora apenas notifica o ViewModel.
                    onDateClick = { date, appointmentsOnDay ->
                        viewModel.onDateClicked(date, appointmentsOnDay)
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
                            // A ação de deletar agora apenas notifica o ViewModel.
                            onDeleteOrClearRequest = { viewModel.onDeleteOrClearRequest(it) }
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
                            // A ação de deletar agora apenas notifica o ViewModel.
                            onDeleteOrClearRequest = { viewModel.onDeleteOrClearRequest(it) }
                        )
                    }
                }
            }
        }
    }
}