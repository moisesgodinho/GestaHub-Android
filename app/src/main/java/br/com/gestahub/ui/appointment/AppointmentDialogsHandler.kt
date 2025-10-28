package br.com.gestahub.ui.appointment

import androidx.compose.runtime.Composable
import br.com.gestahub.ui.components.ConfirmationDialog
import java.time.format.DateTimeFormatter

@Composable
fun AppointmentDialogsHandler(
    dialogState: AppointmentDialogState,
    onDismiss: () -> Unit,
    onNavigateToFormWithDate: (date: String) -> Unit,
    onNavigateToFormWithAppointment: (appointment: Appointment) -> Unit,
    onConfirmDeleteOrClear: (appointment: Appointment) -> Unit
) {
    when (dialogState) {
        is AppointmentDialogState.Hidden -> {
            // Não faz nada, nenhum diálogo é exibido
        }
        is AppointmentDialogState.New -> {
            NewAppointmentDialog(
                date = dialogState.date,
                onDismiss = onDismiss,
                onConfirm = {
                    onNavigateToFormWithDate(it.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            )
        }
        is AppointmentDialogState.View -> {
            ViewAppointmentsDialog(
                date = dialogState.date,
                appointments = dialogState.appointments,
                onDismiss = onDismiss,
                onEdit = onNavigateToFormWithAppointment,
                onDelete = { appointment ->
                    // Ao clicar em deletar no diálogo de visualização, pedimos ao ViewModel para mostrar o diálogo de confirmação.
                    onConfirmDeleteOrClear(appointment)
                },
                onAddNew = {
                    onNavigateToFormWithDate(it.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            )
        }
        is AppointmentDialogState.DeleteOrClear -> {
            val appointment = dialogState.appointment
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
                onConfirm = { onConfirmDeleteOrClear(appointment) },
                onDismissRequest = onDismiss
            )
        }
    }
}