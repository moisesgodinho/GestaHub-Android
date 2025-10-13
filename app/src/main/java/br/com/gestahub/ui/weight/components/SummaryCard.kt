package br.com.gestahub.ui.weight.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun SummaryCard(
    initialBmi: Double,
    currentBmi: Double,
    totalGain: Double,
    gainGoal: String,
    isDarkTheme: Boolean
) {
    val gainOrLossText = if (totalGain >= 0) "Ganho Total" else "Perda Total"
    val totalGainColor = Color(0xFF388E3C)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        BoxWithConstraints {
            val useSingleRow = maxWidth > 600.dp

            if (useSingleRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(label = "IMC Inicial", value = String.format("%.1f", initialBmi), isDarkTheme = isDarkTheme)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(label = "IMC Atual", value = String.format("%.1f", currentBmi), isDarkTheme = isDarkTheme)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(
                            label = gainOrLossText,
                            value = "${String.format("%.1f", abs(totalGain))} kg",
                            isDarkTheme = isDarkTheme,
                            valueColor = totalGainColor
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(label = "Meta de Ganho", value = gainGoal, isDarkTheme = isDarkTheme)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoCard(label = "IMC Inicial", value = String.format("%.1f", initialBmi), isDarkTheme = isDarkTheme)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            InfoCard(label = "IMC Atual", value = String.format("%.1f", currentBmi), isDarkTheme = isDarkTheme)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoCard(
                                label = gainOrLossText,
                                value = "${String.format("%.1f", abs(totalGain))} kg",
                                isDarkTheme = isDarkTheme,
                                valueColor = totalGainColor
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            InfoCard(label = "Meta de Ganho", value = gainGoal, isDarkTheme = isDarkTheme)
                        }
                    }
                }
            }
        }
    }
}