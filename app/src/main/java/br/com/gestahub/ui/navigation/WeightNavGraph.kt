package br.com.gestahub.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import br.com.gestahub.ui.weight.WeightEntryFormScreen
import br.com.gestahub.ui.weight.WeightProfileFormScreen
import br.com.gestahub.ui.weight.WeightScreen
import com.google.accompanist.navigation.animation.composable
import java.time.LocalDate

// Define as animações que serão usadas por este grupo de telas
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.weightGraph(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    isDarkTheme: Boolean,
    estimatedLmp: LocalDate?
) {
    val mainScreenEnterTransition = slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenExitTransition = slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    val mainScreenPopEnterTransition = slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenPopExitTransition = slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))

    val internalScreenEnterTransition = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    val internalScreenExitTransition = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))

    // Tela principal de Peso
    composable(
        "weight",
        enterTransition = { mainScreenEnterTransition },
        exitTransition = { mainScreenExitTransition },
        popEnterTransition = { mainScreenPopEnterTransition },
        popExitTransition = { mainScreenPopExitTransition }
    ) {
        WeightScreen(
            contentPadding = innerPadding,
            isDarkTheme = isDarkTheme,
            onNavigateToProfileForm = { navController.navigate("weight_profile_form") },
            estimatedLmp = estimatedLmp
        )
    }

    // Formulário para adicionar um novo peso
    composable(
        "weight_entry_form",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        WeightEntryFormScreen(
            onNavigateBack = { navController.popBackStack() },
            viewModel = hiltViewModel()
        )
    }

    // Formulário para editar o perfil de peso (altura, peso inicial)
    composable(
        "weight_profile_form",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        WeightProfileFormScreen(
            onNavigateBack = { navController.popBackStack() },
            viewModel = hiltViewModel()
        )
    }
}