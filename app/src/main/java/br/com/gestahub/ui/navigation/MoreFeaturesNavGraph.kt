package br.com.gestahub.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import br.com.gestahub.ui.contractionstimer.ContractionTimerScreen
import br.com.gestahub.ui.home.GestationalDataState
import br.com.gestahub.ui.hydration.HydrationTrackerScreen
import br.com.gestahub.ui.maternitybag.MaternityBagScreen
import br.com.gestahub.ui.medicationtracker.MedicationFormScreen
import br.com.gestahub.ui.medicationtracker.MedicationTrackerScreen
import br.com.gestahub.ui.medicationtracker.MedicationViewModel
import br.com.gestahub.ui.medicationtracker.MedicationViewModelFactory
import br.com.gestahub.ui.movementcounter.MovementCounterScreen
import br.com.gestahub.ui.shoppinglist.ShoppingListScreen
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.moreFeaturesGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    dataState: GestationalDataState
) {
    val internalScreenEnterTransition = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    val internalScreenExitTransition = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))

    composable(
        "movement_counter",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        val lmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
        MovementCounterScreen(
            onNavigateBack = { navController.popBackStack() },
            estimatedLmp = lmp,
            isDarkTheme = isDarkTheme
        )
    }

    composable(
        "maternity_bag",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        MaternityBagScreen(
            onNavigateBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            viewModel = hiltViewModel()
        )
    }

    composable(
        "hydration_tracker",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        HydrationTrackerScreen(
            onNavigateBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            viewModel = hiltViewModel()
        )
    }

    composable(
        "shopping_list",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        ShoppingListScreen(
            navController = navController,
            viewModel = hiltViewModel()
        )
    }

    composable(
        "contraction_timer",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        ContractionTimerScreen(
            onBack = { navController.popBackStack() },
            viewModel = hiltViewModel()
        )
    }

    composable(
        "medication_tracker",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
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
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        MedicationFormScreen(
            onNavigateBack = { navController.popBackStack() },
            viewModel = hiltViewModel()
        )
    }
}