package br.com.gestahub.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.navigation.WeightViewModelFactory
import br.com.gestahub.ui.weight.components.*
import java.time.LocalDate

@Composable
fun WeightScreen(
    contentPadding: PaddingValues,
    isDarkTheme: Boolean,
    onNavigateToProfileForm: () -> Unit,
    estimatedLmp: LocalDate?
) {
    val viewModel: WeightViewModel = viewModel(factory = WeightViewModelFactory(estimatedLmp))
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    val isProfileFilled = profile != null && profile.height > 0 && profile.prePregnancyWeight > 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (!isProfileFilled) {
                InitialProfilePrompt(onAddClick = onNavigateToProfileForm)
            } else {
                ProfileCard(
                    profile = profile!!,
                    isDarkTheme = isDarkTheme,
                    onEditClick = onNavigateToProfileForm
                )
            }
        }

        if (isProfileFilled) {
            item {
                SummaryCard(
                    initialBmi = uiState.initialBmi,
                    currentBmi = uiState.currentBmi,
                    totalGain = uiState.totalGain,
                    gainGoal = uiState.gainGoal,
                    isDarkTheme = isDarkTheme
                )
            }

            if (uiState.weightChartEntries.size > 1) {
                item {
                    WeightGainRecommendationsCard(
                        initialBmi = uiState.initialBmi,
                        isDarkTheme = isDarkTheme
                    )
                }

                item {
                    ChartCard(
                        weightEntries = uiState.weightChartEntries,
                        dateLabels = uiState.chartDateLabels
                    )
                }
            }

            item {
                HistoryCard(
                    uiState = uiState,
                    isDarkTheme = isDarkTheme,
                    onDelete = { entry -> viewModel.deleteWeightEntry(entry) }
                )
            }
        }
    }
}