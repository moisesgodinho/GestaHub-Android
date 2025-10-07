// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalScreen.kt
package br.com.gestahub.ui.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JournalScreen(
    contentPadding: PaddingValues,
    onNavigateToEntry: (date: String) -> Unit
) {
    val viewModel: JournalViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val entriesForMonth by viewModel.entriesForSelectedMonth.collectAsState()

    // --- ESTADO PARA CONTROLAR O DIÁLOGO DE EXCLUSÃO ---
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    // --- DIÁLOGO DE CONFIRMAÇÃO ---
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja apagar este registro do diário? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEntry(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { entryToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        MonthNavigator(
            selectedMonth = selectedMonth,
            onPreviousClick = { viewModel.selectPreviousMonth() },
            onNextClick = { viewModel.selectNextMonth() }
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (entriesForMonth.isEmpty()) {
            EmptyMonthScreen()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entriesForMonth, key = { it.id }) { entry ->
                    JournalItem(
                        entry = entry,
                        onClick = { onNavigateToEntry(entry.date) },
                        onEditClick = { onNavigateToEntry(entry.date) },
                        onDeleteClick = { entryToDelete = it } // Mostra o diálogo ao clicar
                    )
                }
            }
        }
    }
}

@Composable
fun MonthNavigator(
    selectedMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
    val monthText = selectedMonth.format(formatter)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
            }
            Text(
                text = monthText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextClick) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
            }
        }
    }
}

@Composable
fun EmptyMonthScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhum registro encontrado",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Não há registros no diário para este mês. Use as setas para navegar ou adicione um novo registro para hoje.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalItem(
    entry: JournalEntry,
    onClick: () -> Unit,
    onEditClick: (JournalEntry) -> Unit,
    onDeleteClick: (JournalEntry) -> Unit
) {
    val moodsMap = listOf(
        "😄 Feliz", "😌 Tranquila", "🥰 Amorosa", "🎉 Animada", "😴 Cansada",
        "🥱 Sonolenta", "🥺 Sensível", "😟 Ansiosa", "🤔 Preocupada", "😠 Irritada",
        "🤢 Indisposta", "😖 Com dores"
    ).associate {
        val value = it.split(" ").last()
        val label = it
        value to label
    }

    val date = LocalDate.parse(entry.date)
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, dd", Locale("pt", "BR"))
    val formattedDate = date.format(dayFormatter)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onEditClick(entry) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Registro")
                    }
                    IconButton(onClick = { onDeleteClick(entry) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Registro")
                    }
                }
            }

            if (entry.mood.isNotBlank()) {
                val moodLabel = moodsMap[entry.mood] ?: entry.mood
                Text(
                    text = moodLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (entry.symptoms.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sintomas:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        entry.symptoms.forEach { symptom ->
                            SuggestionChip(
                                onClick = { /* Apenas para exibição */ },
                                label = { Text(symptom) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                }
            }

            if (entry.notes.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Anotações:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}