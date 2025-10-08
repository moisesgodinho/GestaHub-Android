// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalScreen.kt
package br.com.gestahub.ui.journal

import androidx.compose.foundation.isSystemInDarkTheme
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
    estimatedLmp: LocalDate?,
    onNavigateToEntry: (date: String) -> Unit
) {
    val viewModel: JournalViewModel = viewModel(factory = JournalViewModel.Factory(estimatedLmp))
    val allEntries by viewModel.allEntries.collectAsState()
    val calendarMonth by viewModel.calendarMonth.collectAsState()
    val entriesForHistoryMonth by viewModel.entriesForHistoryMonth.collectAsState()
    val isNextCalendarMonthEnabled by viewModel.isNextCalendarMonthEnabled.collectAsState()
    val isPreviousCalendarMonthEnabled by viewModel.isPreviousCalendarMonthEnabled.collectAsState()
    val historyMonth by viewModel.historyMonth.collectAsState()
    val isNextHistoryMonthEnabled by viewModel.isNextHistoryMonthEnabled.collectAsState()
    val isPreviousHistoryMonthEnabled by viewModel.isPreviousHistoryMonthEnabled.collectAsState()

    var entryToShow by remember { mutableStateOf<JournalEntry?>(null) }
    var dateToAdd by remember { mutableStateOf<LocalDate?>(null) }
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    entryToShow?.let {
        ViewJournalEntryDialog(
            entry = it,
            onDismiss = { entryToShow = null },
            onEdit = { entry ->
                entryToShow = null
                onNavigateToEntry(entry.date)
            },
            onDelete = { entry ->
                entryToShow = null
                entryToDelete = entry
            }
        )
    }

    dateToAdd?.let {
        NewJournalEntryDialog(
            date = it,
            onDismiss = { dateToAdd = null },
            onConfirm = {
                val dateString = it.format(DateTimeFormatter.ISO_LOCAL_DATE)
                dateToAdd = null
                onNavigateToEntry(dateString)
            }
        )
    }

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

    LazyColumn(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MonthNavigator(
                selectedMonth = calendarMonth,
                onPreviousClick = { viewModel.selectPreviousCalendarMonth() },
                onNextClick = { viewModel.selectNextCalendarMonth() },
                isPreviousEnabled = isPreviousCalendarMonthEnabled,
                isNextEnabled = isNextCalendarMonthEnabled
            )
        }

        item {
            JournalCalendar(
                entries = allEntries,
                displayMonth = calendarMonth,
                minDate = viewModel.minMonth?.atDay(1),
                onDateClick = { date, entry ->
                    if (entry != null) {
                        entryToShow = entry
                    } else {
                        dateToAdd = date
                    }
                }
            )
        }

        item {
            MonthNavigator(
                selectedMonth = historyMonth,
                onPreviousClick = { viewModel.selectPreviousHistoryMonth() },
                onNextClick = { viewModel.selectNextHistoryMonth() },
                isPreviousEnabled = isPreviousHistoryMonthEnabled,
                isNextEnabled = isNextHistoryMonthEnabled
            )
        }

        if (entriesForHistoryMonth.isEmpty()) {
            item {
                EmptyMonthScreen()
            }
        } else {
            items(entriesForHistoryMonth, key = { it.id }) { entry ->
                JournalItem(
                    entry = entry,
                    onEditClick = { onNavigateToEntry(entry.date) },
                    onDeleteClick = { entryToDelete = it }
                )
            }
        }
    }
}

@Composable
fun MonthNavigator(
    selectedMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
    val monthText = selectedMonth.format(formatter)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val cardColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousClick, enabled = isPreviousEnabled) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
            }
            Text(
                text = monthText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextClick, enabled = isNextEnabled) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
            }
        }
    }
}

@Composable
fun EmptyMonthScreen() {
    Column(
        modifier = Modifier.padding(24.dp).fillMaxWidth(),
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
            text = "Não há registros no diário para este mês.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun JournalItem(
    entry: JournalEntry,
    onEditClick: (JournalEntry) -> Unit,
    onDeleteClick: (JournalEntry) -> Unit
) {
    val cardColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- CHAMADA CORRIGIDA: SEM COR DE DESTAQUE ---
                DialogTitle(date = LocalDate.parse(entry.date))
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onEditClick(entry) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Registro")
                    }
                    IconButton(onClick = { onDeleteClick(entry) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Registro")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            JournalItemContent(entry = entry)
        }
    }
}