package br.com.gestahub.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.navigation.WeightViewModelFactory
import br.com.gestahub.ui.theme.Rose500
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.abs


private enum class BmiCategory {
    LOW, NORMAL, OVERWEIGHT, OBESE
}

private fun getBmiCategory(bmi: Double): BmiCategory? {
    return when {
        bmi < 18.5 -> BmiCategory.LOW
        bmi >= 18.5 && bmi < 25.0 -> BmiCategory.NORMAL
        bmi >= 25.0 && bmi < 30.0 -> BmiCategory.OVERWEIGHT
        bmi >= 30.0 -> BmiCategory.OBESE
        else -> null
    }
}


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
                InitialProfilePrompt(onNavigateToProfileForm)
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


// --- CARD DE RECOMENDAÇÕES COM TÍTULO CENTRALIZADO ---
@Composable
fun WeightGainRecommendationsCard(initialBmi: Double, isDarkTheme: Boolean) {
    val currentCategory = getBmiCategory(initialBmi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recomendações de Ganho de Peso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A meta de ganho de peso é baseada no seu IMC pré-gestacional.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RecommendationItem(
                    category = "Baixo Peso",
                    imcRange = "IMC < 18.5",
                    recommendedGain = "12.5 a 18 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.LOW
                )
                RecommendationItem(
                    category = "Peso Adequado",
                    imcRange = "IMC 18.5 - 24.9",
                    recommendedGain = "11.5 a 16 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.NORMAL
                )
                RecommendationItem(
                    category = "Sobrepeso",
                    imcRange = "IMC 25.0 - 29.9",
                    recommendedGain = "7 a 11.5 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.OVERWEIGHT
                )
                RecommendationItem(
                    category = "Obesidade",
                    imcRange = "IMC ≥ 30.0",
                    recommendedGain = "5 a 9 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.OBESE
                )
            }
        }
    }
}


@Composable
private fun RecommendationItem(
    category: String,
    imcRange: String,
    recommendedGain: String,
    isDarkTheme: Boolean,
    isHighlighted: Boolean
) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val categoryStyle = if (isHighlighted) {
        SpanStyle(fontWeight = FontWeight.SemiBold, color = Rose500)
    } else {
        SpanStyle(fontWeight = FontWeight.SemiBold)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (isHighlighted) Rose500 else Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = categoryStyle) {
                            append("$category ")
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append("($imcRange)")
                        }
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ganho de peso total recomendado: $recommendedGain",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// --- CARD DO GRÁFICO COM TÍTULO CENTRALIZADO ---
@Composable
fun ChartCard(
    weightEntries: List<FloatEntry>,
    dateLabels: List<String>
) {
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(key1 = weightEntries) {
        chartEntryModelProducer.setEntries(weightEntries)
    }

    val minY = weightEntries.minByOrNull { it.y }?.y?.minus(2)
    val maxY = weightEntries.maxByOrNull { it.y }?.y?.plus(2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Evolução do Peso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeightChart(
                chartEntryModelProducer = chartEntryModelProducer,
                dateLabels = dateLabels,
                minY = minY,
                maxY = maxY
            )
        }
    }
}

// --- CARD DO HISTÓRICO COM TÍTULO CENTRALIZADO ---
@Composable
fun HistoryCard(
    uiState: WeightUiState,
    isDarkTheme: Boolean,
    onDelete: (WeightEntry) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<WeightEntry?>(null) }

    if (showDialog && entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza de que deseja excluir este registro de peso?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToDelete?.let(onDelete)
                        showDialog = false
                        entryToDelete = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Histórico de Peso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.entries.isEmpty()) {
                EmptyState()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.entries.forEach { entry ->
                        WeightItem(
                            entry = entry,
                            gestationalAge = uiState.gestationalAges[entry.date],
                            isDarkTheme = isDarkTheme,
                            onDelete = {
                                entryToDelete = entry
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileCard(profile: WeightProfile, isDarkTheme: Boolean, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Seus Dados Iniciais",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onEditClick) {
                    Text("Alterar dados")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    InfoCard(
                        label = "Altura",
                        value = "${profile.height} cm",
                        isDarkTheme = isDarkTheme
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    InfoCard(
                        label = "Peso Inicial",
                        value = "${profile.prePregnancyWeight} kg",
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

// --- CARD ATUALIZADO PARA TELAS MAIORES ---
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
            // Define um ponto de corte. Se a largura for maior que 600dp, usa o layout de linha única.
            val useSingleRow = maxWidth > 600.dp

            if (useSingleRow) {
                // Layout para telas grandes (Tablets)
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
                // Layout padrão para telas pequenas (Celulares)
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


@Composable
fun InfoCard(
    label: String,
    value: String,
    isDarkTheme: Boolean,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun InitialProfilePrompt(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Vamos começar!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Para calcular seu IMC e acompanhar seu ganho de peso, precisamos da sua altura e peso antes da gestação.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Text("Adicionar Meus Dados")
            }
        }
    }
}

@Composable
fun WeightItem(
    entry: WeightEntry,
    gestationalAge: String?,
    isDarkTheme: Boolean,
    onDelete: () -> Unit
) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.date.let {
                        try {
                            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            formatter.format(parser.parse(it) ?: Date())
                        } catch (e: Exception) {
                            it
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = gestationalAge ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Rose500
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.1f", entry.weight)} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "IMC: ${String.format("%.1f", entry.bmi)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir peso")
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhum registro de peso",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque no botão '+' para adicionar seu primeiro registro.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}