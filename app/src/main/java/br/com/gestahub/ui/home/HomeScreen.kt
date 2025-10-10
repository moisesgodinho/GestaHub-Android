package br.com.gestahub.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.data.WeeklyInfo

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    homeViewModel: HomeViewModel = viewModel(),
    isDarkTheme: Boolean,
    onAddDataClick: () -> Unit,
    onEditDataClick: () -> Unit
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GestationalInfoDashboard(
                    state = dataState,
                    onEditDataClick = onEditDataClick,
                    isDarkTheme = isDarkTheme
                )
            }
        }
        is GestationalDataState.NoData -> {
            EmptyHomeScreen(onAddDataClick)
        }
    }
}

@Composable
fun EmptyHomeScreen(onAddDataClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhum dado encontrado",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Para começar, adicione a data da sua última menstruação ou os dados do seu último ultrassom.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddDataClick) {
            Text("Adicionar Dados da Gestação")
        }
    }
}

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

@Composable
fun WeeklyInfoCard(info: WeeklyInfo) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Informação Importante", color = MaterialTheme.colorScheme.primary) },
            text = { Text("Estes valores são médias aproximadas. O tamanho e o peso do seu bebê podem variar. O importante é o acompanhamento contínuo no pré-natal.") },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "✨ ${info.title} ✨",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Tamanho comparado a um(a) ${info.size}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${info.length} | ${info.weight}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Mais informações sobre peso e tamanho",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column {
                Text(
                    text = "Bebê:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = info.baby,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column {
                Text(
                    text = "Mamãe:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = info.mom,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    isDarkTheme: Boolean
) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CountdownCard(weeks: Int, days: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF472B6), Color(0xFFFB923C))
                ),
                shape = MaterialTheme.shapes.large
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (weeks >= 0 && days >= 0) {
                val text = if (weeks == 0 && days == 0) "Hoje é o grande dia!" else "${weeks}s ${days}d"
                val subtext = if (weeks == 0 && days == 0) "❤️" else "para o grande dia!"

                Text(
                    text = text,
                    fontSize = if (text.length > 15) 24.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtext,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            } else {
                Text(
                    text = "Bem-vindo(a) ao mundo! ❤️",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}