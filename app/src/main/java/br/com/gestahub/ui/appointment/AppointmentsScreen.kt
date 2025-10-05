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

private const val INITIAL_VISIBLE_PAST_APPOINTMENTS = 5
private const val LOAD_MORE_COUNT = 5

@Composable
fun AppointmentsScreen(
    contentPadding: PaddingValues,
    uiState: AppointmentsUiState,
    onToggleDone: (Appointment) -> Unit,
    onEditClick: (Appointment) -> Unit,
    onDeleteRequest: (Appointment) -> Unit
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))
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
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        if (uiState.upcomingAppointments.isNotEmpty()) {
                            SectionHeader("Próximas Consultas")
                            uiState.upcomingAppointments.forEach { appointment ->
                                AppointmentItem(
                                    appointment = appointment,
                                    lmpDate = uiState.lmpDate, // Passando a DUM
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
                                    lmpDate = uiState.lmpDate, // Passando a DUM
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