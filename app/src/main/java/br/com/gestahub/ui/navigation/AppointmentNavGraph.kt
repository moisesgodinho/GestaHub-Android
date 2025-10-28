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
import br.com.gestahub.ui.appointment.AppointmentFormScreen
import br.com.gestahub.ui.appointment.AppointmentsScreen
import br.com.gestahub.ui.appointment.AppointmentsViewModel
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.appointmentGraph(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    appointmentsViewModel: AppointmentsViewModel,
    isDarkTheme: Boolean
) {
    val mainScreenEnterTransition = slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenExitTransition = slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    val mainScreenPopEnterTransition = slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    val mainScreenPopExitTransition = slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))

    val internalScreenEnterTransition = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    val internalScreenExitTransition = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))

    composable(
        "appointments",
        deepLinks = listOf(navDeepLink { uriPattern = "gestahub://appointments" }),
        enterTransition = { mainScreenEnterTransition },
        exitTransition = { mainScreenExitTransition },
        popEnterTransition = { mainScreenPopEnterTransition },
        popExitTransition = { mainScreenPopExitTransition }
    ) {
        AppointmentsScreen(
            contentPadding = innerPadding,
            viewModel = appointmentsViewModel,
            isDarkTheme = isDarkTheme,
            onNavigateToFormWithDate = { date ->
                navController.navigate("appointmentForm?preselectedDate=$date")
            },
            onNavigateToFormWithAppointment = { appointment ->
                navController.navigate("appointmentForm?appointmentId=${appointment.id}&appointmentType=${appointment.type.name}")
            }
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
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        AppointmentFormScreen(
            onNavigateBack = { navController.popBackStack() },
            viewModel = hiltViewModel()
        )
    }
}