// app/src/main/java/br/com/gestahub/ui/home/HomeScreen.kt
package br.com.gestahub.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.gestahub.ui.home.components.EmptyHomeScreen
import br.com.gestahub.ui.home.components.GestationalInfoDashboard
import br.com.gestahub.ui.home.components.HydrationSummaryCard
import br.com.gestahub.ui.home.components.UpcomingAppointmentsCard

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    homeViewModel: HomeViewModel = viewModel(),
    isDarkTheme: Boolean,
    onAddDataClick: () -> Unit,
    onEditDataClick: (GestationalData) -> Unit,
    navController: NavController
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val dataState = uiState.dataState

    when (dataState) {
        is GestationalDataState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is GestationalDataState.HasData -> {
            var cardsVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                cardsVisible = true
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = cardsVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500))
                ) {
                    GestationalInfoDashboard(
                        state = dataState,
                        onEditDataClick = { onEditDataClick(dataState.gestationalData) },
                        isDarkTheme = isDarkTheme
                    )
                }

                AnimatedVisibility(
                    visible = cardsVisible,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 150)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500, delayMillis = 150))
                ) {
                    HydrationSummaryCard(
                        hydrationData = dataState.todayHydration,
                        onAddWater = { homeViewModel.addWater() },
                        onUndoWater = { homeViewModel.undoLastWater() },
                        onNavigateToTracker = { navController.navigate("hydration_tracker") }
                    )
                }

                if (dataState.upcomingAppointments.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = cardsVisible,
                        enter = fadeIn(animationSpec = tween(500, delayMillis = 250)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500, delayMillis = 250))
                    ) {
                        UpcomingAppointmentsCard(
                            appointments = dataState.upcomingAppointments,
                            navController = navController
                        )
                    }
                }
            }
        }
        is GestationalDataState.NoData -> {
            EmptyHomeScreen(onAddDataClick)
        }
    }
}