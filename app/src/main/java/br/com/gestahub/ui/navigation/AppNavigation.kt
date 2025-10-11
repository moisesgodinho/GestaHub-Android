// Local: app/src/main/java/br/com/gestahub/ui/navigation/AppNavigation.kt
package br.com.gestahub.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.gestahub.ui.appointment.*
import br.com.gestahub.ui.calculator.CalculatorScreen
import br.com.gestahub.ui.components.AppHeader
import br.com.gestahub.ui.home.GestationalDataState
import br.com.gestahub.ui.home.HomeScreen
import br.com.gestahub.ui.journal.JournalEntryScreen
import br.com.gestahub.ui.journal.JournalScreen
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.profile.EditProfileScreen
import br.com.gestahub.ui.profile.ProfileScreen
import br.com.gestahub.ui.weight.WeightScreen
import br.com.gestahub.ui.more.MoreScreen

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestaHubApp(mainViewModel: MainViewModel, user: FirebaseUser) {
    val homeViewModel: br.com.gestahub.ui.home.HomeViewModel = viewModel(key = user.uid)
    val appointmentsViewModel: AppointmentsViewModel = viewModel()

    val homeUiState by homeViewModel.uiState.collectAsState()
    val appointmentsUiState by appointmentsViewModel.uiState.collectAsState()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        NavItem("Início", Icons.Default.Home, "home"),
        NavItem("Consultas", Icons.Default.CalendarMonth, "appointments"),
        NavItem("Diário", Icons.Default.Book, "journal"),
        NavItem("Peso", Icons.Default.BarChart, "weight"),
        NavItem("Mais", Icons.Default.MoreHoriz, "more")
    )

    val showBottomBar = navItems.any { it.route == currentRoute }
    val showMainHeader = showBottomBar

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Appointment?>(null) }
    var showClearDialog by remember { mutableStateOf<Appointment?>(null) }


    LaunchedEffect(appointmentsUiState.userMessage) {
        appointmentsUiState.userMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                appointmentsViewModel.userMessageShown()
            }
        }
    }

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
    LaunchedEffect(key1 = user.uid) {
        homeViewModel.listenToGestationalData(user.uid)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showMainHeader) {
                AppHeader(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { mainViewModel.toggleTheme() },
                    onProfileClick = { navController.navigate("profile") }
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == "appointments") {
                FloatingActionButton(onClick = {
                    navController.navigate("appointmentForm")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Consulta")
                }
            } else if (currentRoute == "journal") {
                FloatingActionButton(onClick = {
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    navController.navigate("journalEntry/$today")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Registro no Diário")
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                HomeScreen(
                    contentPadding = innerPadding,
                    homeViewModel = homeViewModel,
                    isDarkTheme = isDarkTheme,
                    onAddDataClick = { navController.navigate("calculator") },
                    onEditDataClick = {
                        val dataState = homeUiState.dataState
                        if (dataState is GestationalDataState.HasData) {
                            val data = dataState.gestationalData
                            navController.navigate("calculator?lmp=${data.lmp ?: ""}&examDate=${data.ultrasoundExamDate ?: ""}&weeks=${data.weeksAtExam ?: ""}&days=${data.daysAtExam ?: ""}")
                        }
                    }
                )
            }
            composable("appointments") {
                AppointmentsScreen(
                    contentPadding = innerPadding,
                    uiState = appointmentsUiState,
                    isDarkTheme = isDarkTheme,
                    onToggleDone = { appointmentsViewModel.toggleDone(it) },
                    onEditClick = { appointment ->
                        navController.navigate("appointmentForm?appointmentId=${appointment.id}&appointmentType=${appointment.type.name}")
                    },
                    onDeleteOrClearRequest = { appointment ->
                        if (appointment.type == AppointmentType.MANUAL) {
                            showDeleteDialog = appointment
                        } else {
                            showClearDialog = appointment
                        }
                    },
                    onNavigateToForm = { date ->
                        navController.navigate("appointmentForm?preselectedDate=$date")
                    }
                )
            }
            composable("journal") {
                val dataState = homeUiState.dataState
                var lmp: LocalDate? = null
                if (dataState is GestationalDataState.HasData) {
                    lmp = dataState.estimatedLmp
                }
                JournalScreen(
                    contentPadding = innerPadding,
                    estimatedLmp = lmp,
                    onNavigateToEntry = { date ->
                        navController.navigate("journalEntry/$date")
                    }
                )
            }
            composable("weight") { Box(Modifier.padding(innerPadding)) { WeightScreen() } }
            composable("more") { Box(Modifier.padding(innerPadding)) { MoreScreen() } }
            composable(
                route = "appointmentForm?appointmentId={appointmentId}&appointmentType={appointmentType}&preselectedDate={preselectedDate}",
                arguments = listOf(
                    navArgument("appointmentId") { type = NavType.StringType; nullable = true },
                    navArgument("appointmentType") { type = NavType.StringType; nullable = true },
                    navArgument("preselectedDate") { type = NavType.StringType; nullable = true }
                )
            ) {
                AppointmentFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "calculator?lmp={lmp}&examDate={examDate}&weeks={weeks}&days={days}",
                arguments = listOf(
                    navArgument("lmp") { type = NavType.StringType; nullable = true },
                    navArgument("examDate") { type = NavType.StringType; nullable = true },
                    navArgument("weeks") { type = NavType.StringType; nullable = true },
                    navArgument("days") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                CalculatorScreen(
                    onSaveSuccess = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    onCancelClick = { navController.popBackStack() },
                    initialLmp = backStackEntry.arguments?.getString("lmp"),
                    initialExamDate = backStackEntry.arguments?.getString("examDate"),
                    initialWeeks = backStackEntry.arguments?.getString("weeks"),
                    initialDays = backStackEntry.arguments?.getString("days"),
                )
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { navController.navigate("editProfile") }
                )
            }
            composable("editProfile") {
                EditProfileScreen(
                    onSaveSuccess = { navController.popBackStack() },
                    onCancelClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "journalEntry/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) {
                val dataState = homeUiState.dataState
                var lmp: LocalDate? = null
                if (dataState is GestationalDataState.HasData) {
                    lmp = dataState.estimatedLmp
                }

                JournalEntryScreen(
                    estimatedLmp = lmp,
                    onNavigateBack = { navController.popBackStack() },
                    onDateChange = { newDate ->
                        navController.popBackStack()
                        navController.navigate("journalEntry/$newDate")
                    }
                )
            }
        }
    }
}