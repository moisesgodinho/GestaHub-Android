package br.com.gestahub.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import br.com.gestahub.ui.profile.EditProfileScreen
import br.com.gestahub.ui.profile.ProfileScreen
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    val internalScreenEnterTransition = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    val internalScreenExitTransition = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))

    composable(
        "profile",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        ProfileScreen(
            onNavigateBack = { navController.popBackStack() },
            onEditClick = { navController.navigate("editProfile") },
            profileViewModel = hiltViewModel()
        )
    }

    composable(
        "editProfile",
        enterTransition = { internalScreenEnterTransition },
        exitTransition = { internalScreenExitTransition },
        popExitTransition = { internalScreenExitTransition }
    ) {
        EditProfileScreen(
            onSaveSuccess = { navController.popBackStack() },
            onCancelClick = { navController.popBackStack() },
            editProfileViewModel = hiltViewModel()
        )
    }
}