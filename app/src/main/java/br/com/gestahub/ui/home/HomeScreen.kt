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
import br.com.gestahub.ui.home.components.UpcomingAppointmentsCard

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    homeViewModel: HomeViewModel = viewModel(),
    isDarkTheme: Boolean,
    onAddDataClick: () -> Unit,
    onEditDataClick: () -> Unit,
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
            // Estado para controlar a visibilidade dos cards
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
                // O primeiro card aparece imediatamente
                AnimatedVisibility(
                    visible = cardsVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500))
                ) {
                    GestationalInfoDashboard(
                        state = dataState,
                        onEditDataClick = onEditDataClick,
                        isDarkTheme = isDarkTheme
                    )
                }

                // O segundo card aparece um pouco depois, se existir
                if (dataState.upcomingAppointments.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = cardsVisible,
                        enter = fadeIn(animationSpec = tween(500, delayMillis = 200)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500, delayMillis = 200))
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