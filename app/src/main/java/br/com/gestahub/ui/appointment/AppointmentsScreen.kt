// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentsScreen.kt
package br.com.gestahub.ui.appointment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onAddClick: () -> Unit,
    onEditClick: (Appointment) -> Unit,
) {
    val viewModel: AppointmentsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Consultas e Exames") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Consulta")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.upcomingAppointments.isEmpty() && uiState.pastAppointments.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma consulta encontrada. Toque no '+' para adicionar a primeira.",
                            modifier = Modifier.padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (uiState.upcomingAppointments.isNotEmpty()) {
                    item {
                        SectionHeader("Próximas Consultas")
                    }
                    items(uiState.upcomingAppointments, key = { it.id + it.type.name }) { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            onToggleDone = { viewModel.toggleDone(it) },
                            onEdit = { onEditClick(it) }
                        )
                    }
                }

                if (uiState.pastAppointments.isNotEmpty()) {
                    item {
                        SectionHeader("Consultas Passadas")
                    }
                    items(uiState.pastAppointments, key = { it.id + it.type.name }) { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            onToggleDone = { viewModel.toggleDone(it) },
                            onEdit = { onEditClick(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}