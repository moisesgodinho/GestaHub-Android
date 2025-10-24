package br.com.gestahub.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.appointment.Appointment
import br.com.gestahub.ui.appointment.AppointmentsViewModel
import br.com.gestahub.ui.main.MainScreen
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.weight.WeightViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.time.LocalDate

// Factory para criar o WeightViewModel com a DUM
class WeightViewModelFactory(private val estimatedLmp: LocalDate?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeightViewModel(estimatedLmp) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun GestaHubApp(mainViewModel: MainViewModel, user: FirebaseUser) {
    val homeViewModel: br.com.gestahub.ui.home.HomeViewModel = viewModel(key = user.uid)
    val appointmentsViewModel: AppointmentsViewModel = viewModel()

    val appointmentsUiState by appointmentsViewModel.uiState.collectAsState()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Appointment?>(null) }
    var showClearDialog by remember { mutableStateOf<Appointment?>(null) }

    LaunchedEffect(key1 = user.uid) {
        homeViewModel.listenToGestationalData(user.uid)
        // --- NOVA LINHA ADICIONADA ---
        homeViewModel.listenToAppointments(user.uid)
    }

    LaunchedEffect(appointmentsUiState.userMessage) {
        appointmentsUiState.userMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                appointmentsViewModel.userMessageShown()
            }
        }
    }

    // O Scaffold foi movido para a MainScreen, mas os diálogos e snackbar
    // ainda precisam de um host. Vamos envolvê-los aqui por enquanto.
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        MainScreen(
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            appointmentsViewModel = appointmentsViewModel,
            isDarkTheme = isDarkTheme,
            showDeleteDialog = { showDeleteDialog = it },
            showClearDialog = { showClearDialog = it }
        )

        showDeleteDialog?.let { appointment ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Tem certeza que deseja apagar a consulta \"${appointment.title}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            appointmentsViewModel.deleteAppointment(appointment)
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Deletar") }
                },
                dismissButton = { OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") } }
            )
        }

        showClearDialog?.let { appointment ->
            AlertDialog(
                onDismissRequest = { showClearDialog = null },
                title = { Text("Limpar Agendamento") },
                text = { Text("Tem certeza que deseja limpar os dados do agendamento para \"${appointment.title}\"? O item permanecerá na lista.") },
                confirmButton = {
                    Button(onClick = {
                        appointmentsViewModel.clearUltrasoundSchedule(appointment)
                        showClearDialog = null
                    }) { Text("Limpar") }
                },
                dismissButton = { OutlinedButton(onClick = { showClearDialog = null }) { Text("Cancelar") } }
            )
        }
    }
}