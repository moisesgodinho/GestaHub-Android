package br.com.gestahub.ui.contractionstimer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Timer // <- NOVO IMPORT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.components.ConfirmationDialog
import br.com.gestahub.ui.theme.Rose500
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel // Importe esta dependência

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractionTimerScreen(
    onBack: () -> Unit,
    viewModel: ContractionTimerViewModel = hiltViewModel()
) {
    val contractions by viewModel.contractions.collectAsState()
    val isTiming by viewModel.isTiming.collectAsState()
    val timer by viewModel.timer.collectAsState()
    var contractionToDelete by remember { mutableStateOf<Contraction?>(null) }


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
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
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
                    content = "É comum sentir Contrações de Braxton Hicks (de treinamento) a partir do segundo trimestre. Elas são tipicamente irregulares, indolores e não aumentam de intensidade. As contrações de trabalho de parto, por outro lado, tornam-se regulares, mais longas, mais fortes e mais frequentes com o tempo.",
                    borderColor = Color(0xFFF59E0B) // Amarelo/Âmbar
                )
            }
            item {
                InfoCard(
                    title = "Quando ir para a maternidade?",
                    content = "Uma referência comum é a Regra 5-1-1: contrações que duram 1 minuto, ocorrem a cada 5 minutos, por pelo menos 1 hora. No entanto, siga sempre a orientação do seu médico.",
                    borderColor = MaterialTheme.colorScheme.primary
                )
            }

            // --- LÓGICA ATUALIZADA AQUI ---
            item {
                if (contractions.isNotEmpty()) {
                    HistoryCard(
                        contractions = contractions,
                        onDeleteRequest = { contractionToDelete = it }
                    )
                } else {
                    EmptyHistoryState() // Mostra o estado vazio se não houver contrações
                }
            }
        }
    }

    if (contractionToDelete != null) {
        ConfirmationDialog(
            title = "Confirmar Exclusão",
            text = "Tem certeza que deseja apagar este registro de contração?",
            onConfirm = {
                contractionToDelete?.id?.let { viewModel.deleteContraction(it) }
                contractionToDelete = null
            },
            onDismissRequest = { contractionToDelete = null }
        )
    }
}

// --- NOVO COMPONENT ADICIONADO ---
@Composable
fun EmptyHistoryState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhum registro ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quando você cronometrar suas contrações, o histórico completo aparecerá aqui.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ... O restante do código do arquivo permanece o mesmo
@Composable
fun HistoryCard(
    contractions: List<Contraction>,
    onDeleteRequest: (Contraction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Histórico de Contrações",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
            var lastDate: String? = null

            contractions.forEach { contraction ->
                val currentDate = dateFormat.format(contraction.startTime.toDate())
                if (currentDate != lastDate) {
                    DateHeader(date = currentDate)
                    lastDate = currentDate
                }

                ContractionHistoryItem(
                    contraction = contraction,
                    onDelete = { onDeleteRequest(contraction) }
                )
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Column {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Left
        )
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
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
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartStop()
                },
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
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
    val isAppInDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backgroundColor = if (isAppInDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun InfoCard(
    title: String,
    content: String,
    borderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


@Composable
fun ContractionHistoryItem(contraction: Contraction, onDelete: () -> Unit) {
    val isAppInDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val backgroundColor = if (isAppInDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val date = contraction.startTime.toDate()
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
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
            Text(
                text = formatTime(contraction.duration),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Frequência", style = MaterialTheme.typography.bodySmall)
            Text(
                text = formatTime(contraction.frequency),
                color = Rose500,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Apagar")
        }
    }
}


private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}