package br.com.gestahub.ui.medicationtracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationTrackerScreen(
    onBack: () -> Unit,
    viewModel: MedicationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

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
            FloatingActionButton(onClick = { /* TODO: Abrir formulário */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Medicamento")
            }
        }
    ) { paddingValues ->

        val today = LocalDate.now()
        val daysInMonth = (1..currentMonth.lengthOfMonth()).map { currentMonth.withDayOfMonth(it) }

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
                    onMonthChange = { newMonth -> currentMonth = newMonth }
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
                        isHistory = false
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
                            isHistory = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthNavigator(currentMonth: LocalDate, onMonthChange: (LocalDate) -> Unit) {
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
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.ChevronLeft, "Mês anterior")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.titlecase(Locale.getDefault()) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
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
    isHistory: Boolean
) {
    AnimatedVisibility(visible = activeMedications.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                val isToday = date.isEqual(LocalDate.now())
                val formattedDate = date.format(DateTimeFormatter.ofPattern("eeee, dd 'de' MMMM", Locale("pt", "BR")))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isToday) {
                        Chip(label = "Hoje")
                    }
                    Text(text = formattedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    activeMedications.forEach { (med, doses) ->
                        MedicationItem(
                            med = med,
                            doses = doses,
                            takenDoseIndices = historyForDay[med.id] ?: emptyList(),
                            onToggleDose = { doseIndex -> onToggleDose(med.id, date.format(DATE_FORMATTER), doseIndex) },
                            isHistory = isHistory
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
    takenDoseIndices: List<Int>,
    onToggleDose: (Int) -> Unit,
    isHistory: Boolean
) {
    val allDosesTaken = doses.isNotEmpty() && doses.all { takenDoseIndices.contains(it.originalIndex) }
    val isAppInDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val backgroundColor = if (isAppInDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    // V ALTERAÇÃO DE LAYOUT AQUI V
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // A borda só aparece se todas as doses forem tomadas
            if (allDosesTaken) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = med.name,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (allDosesTaken) TextDecoration.LineThrough else TextDecoration.None
                        )
                        med.dosage?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                    }
                    // TODO: Adicionar botões de editar/excluir
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
                                time = doseInfo.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                isTaken = takenDoseIndices.contains(doseInfo.originalIndex),
                                isEnabled = !isHistory,
                                onToggle = { onToggleDose(doseInfo.originalIndex) }
                            )
                        }
                    }
                }
            }
        }
    }
    // ^ FIM DA ALTERAÇÃO ^
}

@Composable
fun DoseItem(time: String, isTaken: Boolean, isEnabled: Boolean, onToggle: () -> Unit) {
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
            )
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = time,
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