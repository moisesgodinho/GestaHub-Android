// Local: app/src/main/java/br/com/gestahub/ui/navigation/AppNavigation.kt
package br.com.gestahub.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.appointment.AppointmentsViewModel
import br.com.gestahub.ui.main.MainScreen
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.weight.WeightViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    // --- CORREÇÃO DEFINITIVA ---
    val homeViewModel: br.com.gestahub.ui.home.HomeViewModel = hiltViewModel()
    val appointmentsViewModel: AppointmentsViewModel = hiltViewModel()

    val appointmentsUiState by appointmentsViewModel.uiState.collectAsState()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = user.uid) {
        homeViewModel.listenToAllData(user.uid)
        appointmentsViewModel.listenToData(user.uid)
    }

    DisposableEffect(Unit) {
        onDispose {
            homeViewModel.clearListeners()
            appointmentsViewModel.clearListeners()
        }
    }

    LaunchedEffect(appointmentsUiState.userMessage) {
        appointmentsUiState.userMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                appointmentsViewModel.userMessageShown()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        MainScreen(
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            appointmentsViewModel = appointmentsViewModel,
            isDarkTheme = isDarkTheme
        )
    }
}