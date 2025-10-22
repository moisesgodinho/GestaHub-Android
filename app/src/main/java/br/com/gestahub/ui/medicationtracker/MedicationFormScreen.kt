// Local: app/src/main/java/br/com/gestahub/ui/medicationtracker/MedicationFormScreen.kt
package br.com.gestahub.ui.medicationtracker

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicationFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, if (uiState.id == null) "Medicamento adicionado!" else "Medicamento atualizado!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id == null) "Adicionar Medicamento" else "Editar Medicamento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    Text("Detalhes", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onFieldChange(name = it) },
                        label = { Text("Nome do Medicamento*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.dosage,
                            onValueChange = { viewModel.onFieldChange(dosage = it) },
                            label = { Text("Dosagem") },
                            placeholder = { Text("Ex: 500mg") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uiState.frequency.toString(),
                            onValueChange = { viewModel.onFieldChange(frequency = it.toIntOrNull()?.coerceIn(1, 10)) },
                            label = { Text("Doses/dia") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            readOnly = uiState.scheduleType == "INTERVAL",
                            enabled = uiState.scheduleType != "INTERVAL"
                        )
                    }
                }

                item {
                    FormSection(title = "Horário") {
                        ScheduleTypeSelector(
                            selected = uiState.scheduleType,
                            onSelect = { viewModel.onFieldChange(scheduleType = it) }
                        )
                        Spacer(Modifier.height(16.dp))
                        when (uiState.scheduleType) {
                            "INTERVAL" -> IntervalScheduleFields(uiState, viewModel)
                            else -> DoseInputFields(uiState, viewModel)
                        }
                    }
                }

                item {
                    FormSection("Duração do Tratamento") {
                        DurationTypeSelector(
                            selected = uiState.durationType,
                            onSelect = { viewModel.onFieldChange(durationType = it) }
                        )
                        if (uiState.durationType == "DAYS") {
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.durationValue,
                                    onValueChange = { viewModel.onFieldChange(durationValue = it) },
                                    label = { Text("Nº de dias") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                DatePickerField(
                                    label = "Data de Início",
                                    dateString = uiState.startDate,
                                    onDateSelected = { viewModel.onFieldChange(startDate = it) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Outros", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onFieldChange(notes = it) },
                        label = { Text("Anotações Adicionais") },
                        placeholder = { Text("Ex: Tomar com bastante água") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = { viewModel.saveMedication() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Salvar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // <-- COR ATUALIZADA
        ) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
private fun ScheduleTypeSelector(selected: String, onSelect: (String) -> Unit) {
    val options = mapOf(
        "FLEXIBLE" to "Flexível",
        "FIXED_TIMES" to "Horário Fixo",
        "INTERVAL" to "Intervalo Fixo"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (key, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSelect(key) }
            ) {
                RadioButton(selected = selected == key, onClick = { onSelect(key) })
                Spacer(Modifier.width(8.dp))
                Text(label)
            }
        }
    }
}

@Composable
private fun DurationTypeSelector(selected: String, onSelect: (String) -> Unit) {
    val options = mapOf(
        "CONTINUOUS" to "Uso Contínuo",
        "DAYS" to "Período de Dias"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        options.forEach { (key, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSelect(key) }
            ) {
                RadioButton(selected = selected == key, onClick = { onSelect(key) })
                Spacer(Modifier.width(8.dp))
                Text(label)
            }
        }
    }
}

@Composable
private fun IntervalScheduleFields(uiState: MedicationFormUiState, viewModel: MedicationFormViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DatePickerField(
            label = "Data de Início*",
            dateString = uiState.startDate,
            onDateSelected = { viewModel.onFieldChange(startDate = it) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimePickerField(
                label = "Horário da 1ª dose*",
                timeString = uiState.doses.firstOrNull() ?: "",
                onTimeSelected = { viewModel.onDoseChange(0, it) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.intervalHours,
                onValueChange = { viewModel.onFieldChange(intervalHours = it) },
                label = { Text("Intervalo (h)*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoseInputFields(uiState: MedicationFormUiState, viewModel: MedicationFormViewModel) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.doses.forEachIndexed { index, dose ->
            val label = "Dose ${index + 1}"
            if (uiState.scheduleType == "FIXED_TIMES") {
                TimePickerField(
                    label = label,
                    timeString = dose,
                    onTimeSelected = { viewModel.onDoseChange(index, it) }
                )
            } else {
                OutlinedTextField(
                    value = dose,
                    onValueChange = { viewModel.onDoseChange(index, it) },
                    label = { Text(label) },
                    placeholder = { Text("Ex: Após o café") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    dateString: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    Box(modifier = modifier.clickable { showDatePicker = true }) {
        OutlinedTextField(
            value = if (dateString.isNotEmpty()) LocalDate.parse(dateString).format(formatter) else "",
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            trailingIcon = { Icon(Icons.Default.DateRange, "Selecionar Data") },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (dateString.isNotEmpty()) {
                LocalDate.parse(dateString).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                Instant.now().toEpochMilli()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                        onDateSelected(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TimePickerField(
    label: String,
    timeString: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.clickable { showTimePicker = true }) {
        OutlinedTextField(
            value = timeString,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            trailingIcon = { Icon(Icons.Default.WatchLater, "Selecionar Horário") },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                onTimeSelected(time)
                showTimePicker = false
            },
            initialTime = if (timeString.isNotBlank()) LocalTime.parse(timeString) else LocalTime.now()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialTime: LocalTime
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    TextButton(onClick = {
                        val selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        onConfirm(selectedTime)
                    }) { Text("OK") }
                }
            }
        }
    }
}