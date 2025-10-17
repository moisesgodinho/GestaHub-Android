package br.com.gestahub.ui.hydration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.components.Header
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationTrackerScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: HydrationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Header(
                title = "Controle de Hidratação",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HydrationTodayCard(
                    todayData = uiState.todayData,
                    onAddWater = viewModel::addWater,
                    onUndo = viewModel::undoLastWater,
                    isDarkTheme = isDarkTheme
                )
            }

            if (uiState.history.isNotEmpty()) {
                item {
                    HistoryCard(
                        history = uiState.history,
                        isLoading = uiState.isLoading,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun HydrationTodayCard(
    todayData: WaterIntakeEntry,
    onAddWater: () -> Unit,
    onUndo: () -> Unit,
    isDarkTheme: Boolean
) {
    val progress = if (todayData.goal > 0) {
        min(todayData.current.toFloat() / todayData.goal.toFloat(), 1f)
    } else {
        0f
    }
    val waterColor = Color(0xFF64B5F6)
    val trackWaterColor = Color(0xFFBBDEFB)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hoje", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Consumo e Meta
            Text(
                text = "${todayData.current} ml",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Meta: ${todayData.goal} ml",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Barra de Progresso
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                color = waterColor,
                trackColor = trackWaterColor
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de Desfazer
                OutlinedButton(
                    onClick = onUndo,
                    enabled = todayData.history.isNotEmpty(),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Desfazer")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Botão de Adicionar
                Button(
                    onClick = onAddWater,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar ${todayData.cupSize} ml", modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Placeholder para manter o alinhamento central
                Spacer(modifier = Modifier.size(56.dp))
            }
            Text(
                text = "+${todayData.cupSize} ml",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun HistoryCard(history: List<WaterIntakeEntry>, isLoading: Boolean, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Histórico",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                history.isEmpty() -> {
                    Text("Nenhum registro de hidratação encontrado.")
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        history.forEach { entry ->
                            HistoryItem(entry = entry, isDarkTheme = isDarkTheme)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(entry: WaterIntakeEntry, isDarkTheme: Boolean) {
    // Formata a string "yyyy-MM-dd" para "dd/MM/yyyy" para exibição
    val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR"))
    val outputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    val dateString = try {
        val dateObject = entry.date?.let { inputFormatter.parse(it) }
        dateObject?.let { outputFormatter.format(it) } ?: "Data desconhecida"
    } catch (e: Exception) {
        entry.id // Fallback para o ID se houver erro
    }

    val progress = if (entry.goal > 0) {
        min(entry.current.toFloat() / entry.goal.toFloat(), 1f)
    } else {
        0f
    }

    val waterColor = Color(0xFF64B5F6)
    val trackWaterColor = Color(0xFFBBDEFB)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateString, // Usando a data reformatada
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${entry.current} / ${entry.goal} ml",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = waterColor,
                    trackColor = trackWaterColor
                )
            }
        }
    }
}