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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import br.com.gestahub.ui.home.GestationalDataState
import br.com.gestahub.ui.journal.JournalEntryScreen
import br.com.gestahub.ui.journal.JournalScreen
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.journalGraph(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    isDarkTheme: Boolean,
    dataState: GestationalDataState
) {
    val mainScreenEnterTransition = slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenExitTransition = slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    val mainScreenPopEnterTransition = slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenPopExitTransition = slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))

    val internalScreenEnterTransition = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    val internalScreenExitTransition = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))

    composable(
        "journal",
        deepLinks = listOf(navDeepLink { uriPattern = "gestahub://journal" }),
        enterTransition = { mainScreenEnterTransition },
        exitTransition = { mainScreenExitTransition },
        popEnterTransition = { mainScreenPopEnterTransition },
        popExitTransition = { mainScreenPopExitTransition }
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
        route = "journalEntry/{date}",
        arguments = listOf(navArgument("date") { type = NavType.StringType }),
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        val lmp = (dataState as? GestationalDataState.HasData)?.estimatedLmp
        JournalEntryScreen(
            estimatedLmp = lmp,
            onNavigateBack = { navController.popBackStack() },
            onDateChange = { newDate ->
                navController.popBackStack()
                navController.navigate("journalEntry/$newDate")
            },
            viewModel = hiltViewModel()
        )
    }
}