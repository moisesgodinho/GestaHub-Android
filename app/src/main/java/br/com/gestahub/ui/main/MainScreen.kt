package br.com.gestahub.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.gestahub.ui.appointment.AppointmentsViewModel
import br.com.gestahub.ui.components.AppHeader
import br.com.gestahub.ui.home.HomeViewModel
import br.com.gestahub.ui.navigation.AppNavGraph
import br.com.gestahub.ui.navigation.navItems
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    appointmentsViewModel: AppointmentsViewModel,
    isDarkTheme: Boolean,
    showDeleteDialog: (br.com.gestahub.ui.appointment.Appointment) -> Unit,
    showClearDialog: (br.com.gestahub.ui.appointment.Appointment) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = navItems.any { it.route == currentRoute }
    val showMainHeader = showBottomBar

    Scaffold(
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
            when (currentRoute) {
                "appointments" -> {
                    FloatingActionButton(onClick = { navController.navigate("appointmentForm") }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Consulta")
                    }
                }
                "journal" -> {
                    FloatingActionButton(onClick = {
                        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        navController.navigate("journalEntry/$today")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Registro no Diário")
                    }
                }
                "weight" -> {
                    FloatingActionButton(onClick = { navController.navigate("weight_entry_form") }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar novo peso")
                    }
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
        AppNavGraph(
            navController = navController,
            innerPadding = innerPadding,
            homeViewModel = homeViewModel,
            appointmentsViewModel = appointmentsViewModel,
            isDarkTheme = isDarkTheme,
            showDeleteDialog = showDeleteDialog,
            showClearDialog = showClearDialog
        )
    }
}