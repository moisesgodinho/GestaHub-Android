package br.com.gestahub.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import br.com.gestahub.ui.appointment.AppointmentsViewModel
import br.com.gestahub.ui.calculator.CalculatorScreen
import br.com.gestahub.ui.home.GestationalDataState
import br.com.gestahub.ui.home.HomeScreen
import br.com.gestahub.ui.home.HomeViewModel
import br.com.gestahub.ui.more.MoreScreen
import br.com.gestahub.ui.onboarding.OnboardingScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    homeViewModel: HomeViewModel,
    appointmentsViewModel: AppointmentsViewModel,
    isDarkTheme: Boolean,
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val dataState = homeUiState.dataState

    // Animações de transição para as telas principais da Bottom Bar
    val mainScreenEnterTransition = slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenExitTransition = slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    val mainScreenPopEnterTransition = slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenPopExitTransition = slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))

    AnimatedNavHost(
        navController = navController,
        startDestination = "decision_route",
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(350)) },
        exitTransition = { fadeOut(animationSpec = tween(350)) }
    ) {
        // --- Rotas de Lógica Inicial ---
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
            route = "calculator?lmp={lmp}&examDate={examDate}&weeks={weeks}&days={days}",
            arguments = listOf(
                navArgument("lmp") { type = NavType.StringType; nullable = true },
                navArgument("examDate") { type = NavType.StringType; nullable = true },
                navArgument("weeks") { type = NavType.StringType; nullable = true },
                navArgument("days") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            CalculatorScreen(
                onSaveSuccess = { navController.navigate("home") { popUpTo("decision_route") { inclusive = true } } },
                onCancelClick = { navController.popBackStack() },
                initialLmp = backStackEntry.arguments?.getString("lmp"),
                initialExamDate = backStackEntry.arguments?.getString("examDate"),
                initialWeeks = backStackEntry.arguments?.getString("weeks"),
                initialDays = backStackEntry.arguments?.getString("days"),
            )
        }

        // --- Rotas da Bottom Bar ---
        composable(
            "home",
            enterTransition = { mainScreenEnterTransition },
            exitTransition = { mainScreenExitTransition },
            popEnterTransition = { mainScreenPopEnterTransition },
            popExitTransition = { mainScreenPopExitTransition }
        ) {
            HomeScreen(
                contentPadding = innerPadding,
                homeViewModel = homeViewModel,
                isDarkTheme = isDarkTheme,
                onAddDataClick = { navController.navigate("calculator") },
                onEditDataClick = { data ->
                    val lmp = data.lmp ?: ""
                    val examDate = data.ultrasoundExamDate ?: ""
                    val weeks = data.weeksAtExam ?: ""
                    val days = data.daysAtExam ?: ""
                    navController.navigate("calculator?lmp=$lmp&examDate=$examDate&weeks=$weeks&days=$days")
                },
                navController = navController
            )
        }

        composable(
            "more",
            enterTransition = { mainScreenEnterTransition },
            exitTransition = { mainScreenExitTransition },
            popEnterTransition = { mainScreenPopEnterTransition },
            popExitTransition = { mainScreenPopExitTransition }
        ) {
            MoreScreen(
                contentPadding = innerPadding,
                onNavigateToMovementCounter = { navController.navigate("movement_counter") },
                onNavigateToMaternityBag = { navController.navigate("maternity_bag") },
                onNavigateToHydrationTracker = { navController.navigate("hydration_tracker") },
                onNavigateToShoppingList = { navController.navigate("shopping_list") },
                onNavigateToContractionTimer = { navController.navigate("contraction_timer") },
                onNavigateToMedicationTracker = { navController.navigate("medication_tracker") }
            )
        }

        // --- Chamadas aos Grafos Aninhados ---
        appointmentGraph(
            navController = navController,
            innerPadding = innerPadding,
            appointmentsViewModel = appointmentsViewModel,
            isDarkTheme = isDarkTheme
        )

        journalGraph(
            navController = navController,
            innerPadding = innerPadding,
            isDarkTheme = isDarkTheme,
            dataState = dataState
        )

        weightGraph(
            navController = navController,
            innerPadding = innerPadding,
            isDarkTheme = isDarkTheme,
            estimatedLmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
        )

        profileGraph(navController = navController)

        moreFeaturesGraph(
            navController = navController,
            isDarkTheme = isDarkTheme,
            dataState = dataState
        )
    }
}