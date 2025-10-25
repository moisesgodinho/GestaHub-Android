// Local: app/src/main/java/br/com/gestahub/ui/navigation/AppNavGraph.kt
package br.com.gestahub.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import br.com.gestahub.ui.appointment.AppointmentFormScreen
import br.com.gestahub.ui.appointment.AppointmentType
import br.com.gestahub.ui.appointment.AppointmentsScreen
import br.com.gestahub.ui.appointment.AppointmentsViewModel
import br.com.gestahub.ui.calculator.CalculatorScreen
import br.com.gestahub.ui.contractionstimer.ContractionTimerScreen
import br.com.gestahub.ui.home.GestationalDataState
import br.com.gestahub.ui.home.HomeScreen
import br.com.gestahub.ui.home.HomeViewModel
import br.com.gestahub.ui.hydration.HydrationTrackerScreen
import br.com.gestahub.ui.journal.JournalEntryScreen
import br.com.gestahub.ui.journal.JournalScreen
import br.com.gestahub.ui.maternitybag.MaternityBagScreen
import br.com.gestahub.ui.medicationtracker.MedicationFormScreen
import br.com.gestahub.ui.medicationtracker.MedicationTrackerScreen
import br.com.gestahub.ui.medicationtracker.MedicationViewModel
import br.com.gestahub.ui.medicationtracker.MedicationViewModelFactory
import br.com.gestahub.ui.more.MoreScreen
import br.com.gestahub.ui.movementcounter.MovementCounterScreen
import br.com.gestahub.ui.onboarding.OnboardingScreen // <- NOVO IMPORT
import br.com.gestahub.ui.profile.EditProfileScreen
import br.com.gestahub.ui.profile.ProfileScreen
import br.com.gestahub.ui.shoppinglist.ShoppingListScreen
import br.com.gestahub.ui.weight.WeightEntryFormScreen
import br.com.gestahub.ui.weight.WeightProfileFormScreen
import br.com.gestahub.ui.weight.WeightScreen
import java.time.LocalDate

@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    homeViewModel: HomeViewModel,
    appointmentsViewModel: AppointmentsViewModel,
    isDarkTheme: Boolean,
    showDeleteDialog: (br.com.gestahub.ui.appointment.Appointment) -> Unit,
    showClearDialog: (br.com.gestahub.ui.appointment.Appointment) -> Unit
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val appointmentsUiState by appointmentsViewModel.uiState.collectAsState()
    val dataState = homeUiState.dataState

    NavHost(
        navController = navController,
        startDestination = "decision_route", // <- MUDANÇA: Ponto de partida agora é a rota de decisão
        modifier = Modifier.fillMaxSize()
    ) {
        // ROTA DE DECISÃO: Verifica se o usuário tem dados e redireciona
        composable("decision_route") {
            // Efeito que executa apenas uma vez quando o estado de dados muda de Loading
            LaunchedEffect(dataState) {
                // Se o estado não for mais de carregamento, tome uma decisão
                if (dataState !is GestationalDataState.Loading) {
                    val destination = if (dataState is GestationalDataState.HasData) "home" else "onboarding"
                    navController.navigate(destination) {
                        // Limpa a pilha de navegação para que o usuário não possa voltar para esta tela de decisão
                        popUpTo("decision_route") { inclusive = true }
                    }
                }
            }

            // Enquanto decide, mostra um indicador de carregamento
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // NOVA ROTA DE ONBOARDING
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToCalculator = { navController.navigate("calculator") }
            )
        }

        composable("home") {
            HomeScreen(
                contentPadding = innerPadding,
                homeViewModel = homeViewModel,
                isDarkTheme = isDarkTheme,
                onAddDataClick = { navController.navigate("calculator") },
                onEditDataClick = {
                    if (dataState is GestationalDataState.HasData) {
                        val data = dataState.gestationalData
                        navController.navigate("calculator?lmp=${data.lmp ?: ""}&examDate=${data.ultrasoundExamDate ?: ""}&weeks=${data.weeksAtExam ?: ""}&days=${data.daysAtExam ?: ""}")
                    }
                },
                navController = navController
            )
        }

        // ... (o restante do seu código do AppNavGraph permanece o mesmo) ...

        composable(
            route = "appointments",
            deepLinks = listOf(navDeepLink { uriPattern = "gestahub://appointments" })
        ) {
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
                        showDeleteDialog(appointment)
                    } else {
                        showClearDialog(appointment)
                    }
                },
                onNavigateToForm = { date ->
                    navController.navigate("appointmentForm?preselectedDate=$date")
                }
            )
        }
        composable(
            "journal",
            deepLinks = listOf(navDeepLink { uriPattern = "gestahub://journal" })
        ) {
            val lmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
            JournalScreen(
                contentPadding = innerPadding,
                estimatedLmp = lmp,
                isDarkTheme = isDarkTheme,
                onNavigateToEntry = { date ->
                    navController.navigate("journalEntry/$date")
                }
            )
        }
        composable("weight") {
            val estimatedLmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp

            WeightScreen(
                contentPadding = innerPadding,
                isDarkTheme = isDarkTheme,
                onNavigateToProfileForm = { navController.navigate("weight_profile_form") },
                estimatedLmp = estimatedLmp
            )
        }
        composable("more") {
            Box(Modifier.padding(innerPadding)) {
                MoreScreen(
                    onNavigateToMovementCounter = { navController.navigate("movement_counter") },
                    onNavigateToMaternityBag = { navController.navigate("maternity_bag") },
                    onNavigateToHydrationTracker = { navController.navigate("hydration_tracker") },
                    onNavigateToShoppingList = { navController.navigate("shopping_list") },
                    onNavigateToContractionTimer = { navController.navigate("contraction_timer") },
                    onNavigateToMedicationTracker = { navController.navigate("medication_tracker") }
                )
            }
        }
        composable(
            route = "appointmentForm?appointmentId={appointmentId}&appointmentType={appointmentType}&preselectedDate={preselectedDate}",
            arguments = listOf(
                navArgument("appointmentId") { type = NavType.StringType; nullable = true },
                navArgument("appointmentType") { type = NavType.StringType; nullable = true },
                navArgument("preselectedDate") { type = NavType.StringType; nullable = true }
            )
        ) {
            AppointmentFormScreen(onNavigateBack = { navController.popBackStack() })
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
                onSaveSuccess = {
                    // MUDANÇA: Após salvar, vai para a home e limpa a pilha até a rota de decisão
                    navController.navigate("home") {
                        popUpTo("decision_route") { inclusive = true }
                    }
                },
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
            val lmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
            JournalEntryScreen(
                estimatedLmp = lmp,
                onNavigateBack = { navController.popBackStack() },
                onDateChange = { newDate ->
                    navController.popBackStack()
                    navController.navigate("journalEntry/$newDate")
                }
            )
        }
        composable("weight_entry_form") {
            WeightEntryFormScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("weight_profile_form") {
            WeightProfileFormScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("movement_counter") {
            val lmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
            MovementCounterScreen(
                onNavigateBack = { navController.popBackStack() },
                estimatedLmp = lmp,
                isDarkTheme = isDarkTheme
            )
        }
        composable("maternity_bag") {
            MaternityBagScreen(
                onNavigateBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme
            )
        }
        composable("hydration_tracker") {
            HydrationTrackerScreen(
                onNavigateBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme
            )
        }
        composable("shopping_list") {
            ShoppingListScreen(
                navController = navController
            )
        }
        composable("contraction_timer") {
            ContractionTimerScreen(onBack = { navController.popBackStack() })
        }
        composable("medication_tracker") {
            val estimatedLmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
            val medicationViewModelFactory = MedicationViewModelFactory(estimatedLmp)
            val medicationViewModel: MedicationViewModel = viewModel(factory = medicationViewModelFactory)

            MedicationTrackerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToForm = { medId ->
                    val route = if (medId != null) "medicationForm?medicationId=$medId" else "medicationForm"
                    navController.navigate(route)
                },
                viewModel = medicationViewModel
            )
        }
        composable(
            route = "medicationForm?medicationId={medicationId}",
            arguments = listOf(navArgument("medicationId") {
                type = NavType.StringType
                nullable = true
            })
        ) {
            MedicationFormScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}