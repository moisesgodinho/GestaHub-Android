package br.com.gestahub.ui.contractionstimer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractionTimerScreen(
    onBack: () -> Unit,
    viewModel: ContractionTimerViewModel = viewModel()
) {
    val contractions by viewModel.contractions.collectAsState()
    val isTiming by viewModel.isTiming.collectAsState()
    val timer by viewModel.timer.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronômetro de Contrações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TimerCard(
                    timer = timer,
                    isTiming = isTiming,
                    lastContraction = contractions.firstOrNull(),
                    onStartStop = viewModel::handleStartStop
                )
            }
            item {
                InfoCard(
                    title = "Contrações de Treinamento vs. Trabalho de Parto",
                    content = "É comum sentir Contrações de Braxton Hicks (de treinamento) a partir do segundo trimestre. Elas são tipicamente irregulares, indolores e não aumentam de intensidade. As contrações de trabalho de parto, por outro lado, tornam-se regulares, mais longas, mais fortes e mais frequentes com o tempo."
                )
            }
            item {
                InfoCard(
                    title = "Quando ir para a maternidade?",
                    content = "Uma referência comum é a Regra 5-1-1: contrações que duram 1 minuto, ocorrem a cada 5 minutos, por pelo menos 1 hora. No entanto, siga sempre a orientação do seu médico."
                )
            }
            if (contractions.isNotEmpty()) {
                item {
                    Text(
                        "Histórico de Contrações",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                items(contractions) { contraction ->
                    ContractionHistoryItem(
                        contraction = contraction,
                        onDelete = { showDeleteDialog = it }
                    )
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
    showDeleteDialog?.let { contractionId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(text = "Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja apagar este registro de contração?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteContraction(contractionId)
                    showDeleteDialog = null
                }) {
                    Text("Apagar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TimerCard(
    timer: Long,
    isTiming: Boolean,
    lastContraction: Contraction?,
    onStartStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quando a contração começar, pressione \"Iniciar\". Quando terminar, pressione \"Parar\".",
                textAlign = TextAlign.Center
            )
            Text(
                text = formatTime(timer),
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onStartStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isTiming) "Parar Contração" else "Iniciar Contração", fontSize = 18.sp)
            }
            lastContraction?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    SummaryItem("Última Duração", formatTime(it.duration), modifier = Modifier.weight(1f))
                    SummaryItem("Última Frequência", formatTime(it.frequency), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
fun ContractionHistoryItem(contraction: Contraction, onDelete: (String) -> Unit) {
    val date = contraction.startTime.toDate()
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Início", style = MaterialTheme.typography.bodySmall)
            Text(
                text = timeFormat.format(date),
                fontWeight = FontWeight.Bold
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Duração", style = MaterialTheme.typography.bodySmall)
            Text(formatTime(contraction.duration))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Frequência", style = MaterialTheme.typography.bodySmall)
            Text(formatTime(contraction.frequency))
        }
        IconButton(onClick = { onDelete(contraction.id) }) {
            Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = Color.Gray)
        }
    }
}


private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}