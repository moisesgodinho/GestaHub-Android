package br.com.gestahub.ui.hydration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        WaterSettingsDialog(
            currentGoal = uiState.todayData.goal,
            currentCupSize = uiState.todayData.cupSize,
            onDismiss = { showSettingsDialog = false },
            onSave = { newGoal, newCupSize ->
                viewModel.setWaterSettings(newGoal, newCupSize)
                showSettingsDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            Header(
                title = "Controle de Hidratação",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        // --- CORREÇÃO APLICADA AQUI ---
        // Verifica o estado de 'isLoading' antes de mostrar o conteúdo
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                        onAddCustomAmount = viewModel::addCustomAmount,
                        onEditSettings = { showSettingsDialog = true },
                        isDarkTheme = isDarkTheme
                    )
                }

                item {
                    InfoCardWithLeftBorder()
                }
            }
        }
    }
}

@Composable
fun InfoCardWithLeftBorder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Spacer(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "A Importância da Hidratação na Gestação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Manter-se bem hidratada durante a gravidez é fundamental para a sua saúde e para o desenvolvimento do bebê. A água ajuda a formar o líquido amniótico, produzir mais volume sanguíneo, construir novos tecidos, transportar nutrientes e eliminar toxinas.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Recomendação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    buildAnnotatedString {
                        append("A recomendação geral é consumir de ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("2 a 3 litros")
                        }
                        append(" de líquidos por dia, o que equivale a cerca de 8 a 12 copos. No entanto, essa necessidade pode variar. Converse sempre com seu médico para entender a quantidade ideal para você.")
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun HydrationTodayCard(
    todayData: WaterIntakeEntry,
    onAddWater: () -> Unit,
    onUndo: () -> Unit,
    onAddCustomAmount: (Int) -> Unit,
    onEditSettings: () -> Unit,
    isDarkTheme: Boolean
) {
    val progress = if (todayData.goal > 0) min(todayData.current.toFloat() / todayData.goal.toFloat(), 1f) else 0f
    val waterColor = Color(0xFF64B5F6)
    val trackWaterColor = Color(0xFFBBDEFB)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hoje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onEditSettings) {
                    Text("Editar meta e copo")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
            }
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                color = waterColor,
                trackColor = trackWaterColor
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onUndo,
                    enabled = todayData.history.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remover último")
                }
                Button(
                    onClick = onAddWater,
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Adicionar um copo (${todayData.cupSize} ml)")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Adicionar rápido:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val quickAmounts = listOf(50, 100, 150, 200)
                quickAmounts.forEach { amount ->
                    Button(
                        onClick = { onAddCustomAmount(amount) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = waterColor)
                    ) {
                        Text("+$amount ml")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterSettingsDialog(
    currentGoal: Int,
    currentCupSize: Int,
    onDismiss: () -> Unit,
    onSave: (goal: Int, cupSize: Int) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }
    var cupSizeText by remember { mutableStateOf(currentCupSize.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurações de Hidratação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Meta diária (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = cupSizeText,
                    onValueChange = { cupSizeText = it },
                    label = { Text("Tamanho do copo (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newGoal = goalText.toIntOrNull() ?: currentGoal
                val newCupSize = cupSizeText.toIntOrNull() ?: currentCupSize
                onSave(newGoal, newCupSize)
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}