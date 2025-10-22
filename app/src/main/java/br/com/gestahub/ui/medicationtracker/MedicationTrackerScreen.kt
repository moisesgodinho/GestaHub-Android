// Local: app/src/main/java/br/com/gestahub/ui/medicationtracker/MedicationTrackerScreen.kt
package br.com.gestahub.ui.medicationtracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

// --- CORREÇÃO: Visibilidade do enum alterada para pública (removendo o modificador 'internal') ---
enum class DeleteOption { END_TODAY, DELETE_ALL }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationTrackerScreen(
    onBack: () -> Unit,
    onNavigateToForm: (medId: String?) -> Unit,
    viewModel: MedicationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val isNextMonthEnabled by viewModel.isNextMonthEnabled.collectAsState()
    val isPreviousMonthEnabled by viewModel.isPreviousMonthEnabled.collectAsState()
    var medToDelete by remember { mutableStateOf<Medication?>(null) }


    if (medToDelete != null) {
        DeleteMedicationDialog(
            medicationName = medToDelete!!.name,
            onDismiss = { medToDelete = null },
            onConfirm = { option ->
                when (option) {
                    DeleteOption.END_TODAY -> viewModel.endMedicationFromToday(medToDelete!!)
                    DeleteOption.DELETE_ALL -> viewModel.deleteMedication(medToDelete!!.id)
                }
                medToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Controle de Medicamentos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Medicamento")
            }
        }
    ) { paddingValues ->
        val today = LocalDate.now()
        val daysInMonth = (1..currentMonth.lengthOfMonth()).map { currentMonth.atDay(it) }

        val monthlyActiveMeds by remember(currentMonth, uiState.medications) {
            derivedStateOf {
                daysInMonth.associateWith { date ->
                    uiState.medications.mapNotNull { med ->
                        if (isMedicationActiveOnDate(med, date)) {
                            val doses = getDosesForDay(med, date)
                            if (doses.isNotEmpty()) med to doses else null
                        } else {
                            null
                        }
                    }
                }
            }
        }

        val completedDays by remember(monthlyActiveMeds, uiState.history) {
            derivedStateOf {
                monthlyActiveMeds.filter { (date, activeMeds) ->
                    if (activeMeds.isEmpty()) return@filter false
                    val historyForDay = uiState.history[date.format(DATE_FORMATTER)] ?: emptyMap()
                    activeMeds.all { (med, doses) ->
                        val takenDoseIndices = historyForDay[med.id] ?: emptyList()
                        doses.all { doseInfo -> takenDoseIndices.contains(doseInfo.originalIndex) }
                    }
                }.keys
            }
        }

        val upcomingDays = daysInMonth.filter { !it.isBefore(today) }
        val pastDays = daysInMonth.filter { it.isBefore(today) }.sortedDescending()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthNavigator(
                    currentMonth = currentMonth,
                    onPreviousClick = viewModel::selectPreviousMonth,
                    onNextClick = viewModel::selectNextMonth,
                    isPreviousEnabled = isPreviousMonthEnabled,
                    isNextEnabled = isNextMonthEnabled
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.medications.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Você ainda não adicionou nenhum medicamento ou vitamina.",
                            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                }
            } else {
                items(upcomingDays, key = { it.toEpochDay() }) { date ->
                    DayMedicationCard(
                        date = date,
                        activeMedications = monthlyActiveMeds[date] ?: emptyList(),
                        historyForDay = uiState.history[date.format(DATE_FORMATTER)] ?: emptyMap(),
                        onToggleDose = viewModel::toggleDose,
                        isComplete = date in completedDays,
                        isHistory = false,
                        onEdit = onNavigateToForm,
                        onDelete = { medToDelete = it }
                    )
                }

                if (pastDays.any { monthlyActiveMeds[it]?.isNotEmpty() == true }) {
                    item {
                        Text(
                            text = "Histórico do Mês",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(pastDays, key = { it.toEpochDay() }) { date ->
                        DayMedicationCard(
                            date = date,
                            activeMedications = monthlyActiveMeds[date] ?: emptyList(),
                            historyForDay = uiState.history[date.format(DATE_FORMATTER)] ?: emptyMap(),
                            onToggleDose = viewModel::toggleDose,
                            isComplete = date in completedDays,
                            isHistory = true,
                            onEdit = onNavigateToForm,
                            onDelete = { medToDelete = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteMedicationDialog(
    medicationName: String,
    onDismiss: () -> Unit,
    onConfirm: (DeleteOption) -> Unit
) {
    var selectedOption by remember { mutableStateOf(DeleteOption.END_TODAY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Opções para \"$medicationName\"") },
        text = {
            Column {
                Text("Escolha o que você quer fazer com este medicamento:")
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = DeleteOption.END_TODAY }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedOption == DeleteOption.END_TODAY,
                            onClick = { selectedOption = DeleteOption.END_TODAY }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Encerrar a partir de hoje (manter histórico)", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = DeleteOption.DELETE_ALL }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedOption == DeleteOption.DELETE_ALL,
                            onClick = { selectedOption = DeleteOption.DELETE_ALL }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Excluir tudo (apagar medicamento e histórico)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedOption) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun MonthNavigator(
    currentMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousClick, enabled = isPreviousEnabled) {
                Icon(Icons.Default.ChevronLeft, "Mês anterior")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.titlecase(Locale.getDefault()) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextClick, enabled = isNextEnabled) {
                Icon(Icons.Default.ChevronRight, "Próximo mês")
            }
        }
    }
}


@Composable
fun DayMedicationCard(
    date: LocalDate,
    activeMedications: List<Pair<Medication, List<DoseInfo>>>,
    historyForDay: Map<String, List<Int>>,
    onToggleDose: (String, String, Int) -> Unit,
    isComplete: Boolean,
    isHistory: Boolean,
    onEdit: (medId: String) -> Unit,
    onDelete: (Medication) -> Unit
) {
    AnimatedVisibility(visible = activeMedications.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                if (isComplete) {
                    Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
                }
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isToday = date.isEqual(LocalDate.now())
                    val formattedDate = date.format(DateTimeFormatter.ofPattern("eeee, dd 'de' MMMM", Locale("pt", "BR")))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isToday) Chip(label = "Hoje")
                        Text(text = formattedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    activeMedications.forEach { (med, doses) ->
                        MedicationItem(
                            med = med,
                            doses = doses,
                            date = date,
                            takenDoseIndices = historyForDay[med.id] ?: emptyList(),
                            onToggleDose = { doseIndex -> onToggleDose(med.id, date.format(DATE_FORMATTER), doseIndex) },
                            isHistory = isHistory,
                            isDayComplete = isComplete,
                            onEdit = { onEdit(med.id) },
                            onDelete = { onDelete(med) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationItem(
    med: Medication,
    doses: List<DoseInfo>,
    date: LocalDate,
    takenDoseIndices: List<Int>,
    onToggleDose: (Int) -> Unit,
    isHistory: Boolean,
    isDayComplete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val allDosesTaken = doses.isNotEmpty() && doses.all { takenDoseIndices.contains(it.originalIndex) }
    val isAppInDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backgroundColor = if (isAppInDarkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(backgroundColor)) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            if (allDosesTaken && !isDayComplete) {
                Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = med.name,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (allDosesTaken) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(Modifier.width(8.dp))
                        val pillText = remember(med, date) {
                            when (med.durationType) {
                                "CONTINUOUS" -> "Uso contínuo"
                                "DAYS" -> {
                                    try {
                                        val startDate = LocalDate.parse(med.startDate, DATE_FORMATTER)
                                        val duration = med.durationValue ?: 0L
                                        if (duration > 0) {
                                            val endDate = startDate.plusDays(duration - 1)
                                            when {
                                                date.isEqual(endDate) -> "Último dia"
                                                date.isBefore(endDate) -> {
                                                    val remaining = ChronoUnit.DAYS.between(date, endDate)
                                                    if (remaining > 0) "$remaining dias restantes" else ""
                                                }
                                                else -> ""
                                            }
                                        } else ""
                                    } catch (e: Exception) { "" }
                                }
                                else -> ""
                            }
                        }
                        if (pillText.isNotEmpty()) {
                            MedicationPill(text = pillText)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Medicamento", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir Medicamento", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                med.notes?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }

                if (doses.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        doses.forEach { doseInfo ->
                            DoseItem(
                                description = doseInfo.description,
                                isTaken = takenDoseIndices.contains(doseInfo.originalIndex),
                                isEnabled = date.isEqual(LocalDate.now()),
                                onToggle = { onToggleDose(doseInfo.originalIndex) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationPill(text: String) {
    val backgroundColor = when {
        text == "Último dia" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        text == "Último dia" -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DoseItem(
    description: String,
    isTaken: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isTaken,
            onCheckedChange = { onToggle() },
            enabled = isEnabled,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledCheckedColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                disabledUncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.scale(1.2f)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = description,
            textDecoration = if (isTaken) TextDecoration.LineThrough else TextDecoration.None,
            color = if (isEnabled) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
fun Chip(label: String) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}