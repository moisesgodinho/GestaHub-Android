package br.com.gestahub.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun JournalScreen(
    contentPadding: PaddingValues,
    estimatedLmp: LocalDate?,
    isDarkTheme: Boolean,
    onNavigateToEntry: (date: String) -> Unit
) {
    val viewModel: JournalViewModel = hiltViewModel()

    // Este LaunchedEffect garante que o ViewModel seja inicializado com a data
    // assim que ela estiver disponível, e apenas uma vez.
    LaunchedEffect(estimatedLmp) {
        viewModel.initialize(estimatedLmp)
    }

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

    var itemsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(entriesForHistoryMonth) {
        itemsVisible = true
    }

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
            JournalCalendar(
                entries = allEntries,
                displayMonth = calendarMonth,
                minDate = viewModel.estimatedLmp, // Acesso corrigido
                onDateClick = { date, entry ->
                    if (entry != null) {
                        entryToShow = entry
                    } else {
                        dateToAdd = date
                    }
                },
                onPreviousClick = { viewModel.selectPreviousCalendarMonth() },
                onNextClick = { viewModel.selectNextCalendarMonth() },
                isPreviousEnabled = isPreviousCalendarMonthEnabled,
                isNextEnabled = isNextCalendarMonthEnabled,
                isDarkTheme = isDarkTheme
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Histórico do Diário",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MonthNavigator(
                        selectedMonth = historyMonth,
                        onPreviousClick = { viewModel.selectPreviousHistoryMonth() },
                        onNextClick = { viewModel.selectNextHistoryMonth() },
                        isPreviousEnabled = isPreviousHistoryMonthEnabled,
                        isNextEnabled = isNextHistoryMonthEnabled,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (entriesForHistoryMonth.isEmpty()) {
                        EmptyMonthScreen()
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            entriesForHistoryMonth.forEachIndexed { index, entry ->
                                AnimatedVisibility(
                                    visible = itemsVisible,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = index * 100))
                                            + slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(durationMillis = 500, delayMillis = index * 100)
                                    )
                                ) {
                                    JournalItem(
                                        entry = entry,
                                        onEditClick = { onNavigateToEntry(entry.date) },
                                        onDeleteClick = { entryToDelete = it },
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            }
                        }
                    }
                }
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
    isNextEnabled: Boolean,
    isDarkTheme: Boolean,
    title: String? = null
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
    val monthText = selectedMonth.format(formatter)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val cardColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }
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
}

@Composable
fun EmptyMonthScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Book,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum registro neste mês",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use o calendário acima para adicionar um registro ou navegue para outros meses para ver seu histórico.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun JournalItem(
    entry: JournalEntry,
    onEditClick: (JournalEntry) -> Unit,
    onDeleteClick: (JournalEntry) -> Unit,
    isDarkTheme: Boolean
) {
    val cardColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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