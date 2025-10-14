package br.com.gestahub.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.home.GestationalDataState

@Composable
fun GestationalInfoDashboard(
    state: GestationalDataState.HasData,
    onEditDataClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sua Gestação",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onEditDataClick) {
                    Text("Alterar Dados")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints {
                if (maxWidth > 600.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoCard(
                                label = "Idade Gestacional",
                                value = "${state.gestationalWeeks}s ${state.gestationalDays}d",
                                isDarkTheme = isDarkTheme
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            InfoCard(
                                label = "Data Provável do Parto",
                                value = state.dueDate,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoCard(
                            label = "Idade Gestacional",
                            value = "${state.gestationalWeeks}s ${state.gestationalDays}d",
                            isDarkTheme = isDarkTheme
                        )
                        InfoCard(
                            label = "Data Provável do Parto",
                            value = state.dueDate,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CountdownCard(weeks = state.countdownWeeks, days = state.countdownDays)

            state.weeklyInfo?.let { info ->
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyInfoCard(info = info)
            }
        }
    }
}