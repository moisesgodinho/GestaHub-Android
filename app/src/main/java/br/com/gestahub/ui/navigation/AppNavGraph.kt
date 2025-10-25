// Local: app/src/main/java/br/com/gestahub/ui/navigation/AppNavGraph.kt
package br.com.gestahub.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import br.com.gestahub.ui.onboarding.OnboardingScreen
import br.com.gestahub.ui.profile.EditProfileScreen
import br.com.gestahub.ui.profile.ProfileScreen
import br.com.gestahub.ui.shoppinglist.ShoppingListScreen
import br.com.gestahub.ui.weight.WeightEntryFormScreen
import br.com.gestahub.ui.weight.WeightProfileFormScreen
import br.com.gestahub.ui.weight.WeightScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class)
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

    val mainScreenEnterTransition = slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenExitTransition = slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    val mainScreenPopEnterTransition = slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenPopExitTransition = slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))

    val internalScreenEnterTransition = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    val internalScreenExitTransition = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))

    AnimatedNavHost(
        navController = navController,
        startDestination = "decision_route",
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(350)) },
        exitTransition = { fadeOut(animationSpec = tween(350)) }
    ) {
        composable("decision_route") {
            LaunchedEffect(dataState) {
                if (dataState !is GestationalDataState.Loading) {
                    val destination = if (dataState is GestationalDataState.HasData) "home" else "onboarding"
                    navController.navigate(destination) {
                        popUpTo("decision_route") { inclusive = true }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("onboarding") {
            OnboardingScreen(
                onNavigateToCalculator = { navController.navigate("calculator") }
            )
        }

        composable(
            "home",
            enterTransition = { mainScreenEnterTransition }, exitTransition = { mainScreenExitTransition },
            popEnterTransition = { mainScreenPopEnterTransition }, popExitTransition = { mainScreenPopExitTransition }
        ) {
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

        composable(
            "appointments", deepLinks = listOf(navDeepLink { uriPattern = "gestahub://appointments" }),
            enterTransition = { mainScreenEnterTransition }, exitTransition = { mainScreenExitTransition },
            popEnterTransition = { mainScreenPopEnterTransition }, popExitTransition = { mainScreenPopExitTransition }
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
            "journal", deepLinks = listOf(navDeepLink { uriPattern = "gestahub://journal" }),
            enterTransition = { mainScreenEnterTransition }, exitTransition = { mainScreenExitTransition },
            popEnterTransition = { mainScreenPopEnterTransition }, popExitTransition = { mainScreenPopExitTransition }
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

        composable(
            "weight",
            enterTransition = { mainScreenEnterTransition }, exitTransition = { mainScreenExitTransition },
            popEnterTransition = { mainScreenPopEnterTransition }, popExitTransition = { mainScreenPopExitTransition }
        ) {
            val estimatedLmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
            WeightScreen(
                contentPadding = innerPadding,
                isDarkTheme = isDarkTheme,
                onNavigateToProfileForm = { navController.navigate("weight_profile_form") },
                estimatedLmp = estimatedLmp
            )
        }

        composable(
            "more",
            enterTransition = { mainScreenEnterTransition }, exitTransition = { mainScreenExitTransition },
            popEnterTransition = { mainScreenPopEnterTransition }, popExitTransition = { mainScreenPopExitTransition }
        ) {
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
            route = "calculator?lmp={lmp}&examDate={examDate}&weeks={weeks}&days={days}",
            arguments = listOf(
                navArgument("lmp") { type = NavType.StringType; nullable = true },
                navArgument("examDate") { type = NavType.StringType; nullable = true },
                navArgument("weeks") { type = NavType.StringType; nullable = true },
                navArgument("days") { type = NavType.StringType; nullable = true }
            ),
            enterTransition = { internalScreenEnterTransition },
            exitTransition = { internalScreenExitTransition }
        ) { backStackEntry ->
            CalculatorScreen(
                onSaveSuccess = {
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

        composable(
            route = "appointmentForm?appointmentId={appointmentId}&appointmentType={appointmentType}&preselectedDate={preselectedDate}",
            arguments = listOf(
                navArgument("appointmentId") { type = NavType.StringType; nullable = true },
                navArgument("appointmentType") { type = NavType.StringType; nullable = true },
                navArgument("preselectedDate") { type = NavType.StringType; nullable = true }
            ),
            enterTransition = { internalScreenEnterTransition },
            exitTransition = { internalScreenExitTransition }
        ) {
            AppointmentFormScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = "journalEntry/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType }),
            enterTransition = { internalScreenEnterTransition },
            exitTransition = { internalScreenExitTransition }
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

        val internalExitPop = internalScreenExitTransition

        composable("profile", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() }, onEditClick = { navController.navigate("editProfile") })
        }
        composable("editProfile", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            EditProfileScreen(onSaveSuccess = { navController.popBackStack() }, onCancelClick = { navController.popBackStack() })
        }
        composable("weight_entry_form", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            WeightEntryFormScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("weight_profile_form", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            WeightProfileFormScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("movement_counter", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            val lmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
            MovementCounterScreen(onNavigateBack = { navController.popBackStack() }, estimatedLmp = lmp, isDarkTheme = isDarkTheme)
        }
        composable("maternity_bag", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            MaternityBagScreen(onNavigateBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme)
        }
        composable("hydration_tracker", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            HydrationTrackerScreen(onNavigateBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme)
        }
        composable("shopping_list", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            ShoppingListScreen(navController = navController)
        }
        composable("contraction_timer", enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }) {
            ContractionTimerScreen(onBack = { navController.popBackStack() })
        }
        composable(
            "medication_tracker",
            enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }
        ) {
            val estimatedLmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
            val medicationViewModel: MedicationViewModel = viewModel(factory = MedicationViewModelFactory(estimatedLmp))
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
            "medicationForm?medicationId={medicationId}",
            arguments = listOf(navArgument("medicationId") { type = NavType.StringType; nullable = true }),
            enterTransition = { internalScreenEnterTransition }, exitTransition = { internalScreenExitTransition }, popExitTransition = { internalExitPop }
        ) {
            MedicationFormScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}