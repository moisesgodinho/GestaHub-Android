// Local: app/src/main/java/br/com/gestahub/ui/appointment/AppointmentFormScreen.kt
package br.com.gestahub.ui.appointment

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val NOTES_MAX_LENGTH = 500

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppointmentFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppointmentFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Consulta salva com sucesso!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.userMessageShown()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.id == null) "Nova Consulta" else "Editar Consulta")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onFieldChange(title = it) },
                        label = { Text("Título*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isUltrasound
                    )

                    if (!uiState.isUltrasound) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppointmentData.appointmentTypes.forEach { suggestion ->
                                SuggestionChip(
                                    onClick = { viewModel.onFieldChange(title = suggestion) },
                                    label = { Text(suggestion) }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DatePickerField(
                                label = "Data*",
                                dateString = uiState.date,
                                onDateSelected = { viewModel.onFieldChange(date = it) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TimePickerField(
                                label = "Hora",
                                timeString = uiState.time,
                                onTimeSelected = { viewModel.onFieldChange(time = it) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.professional,
                        onValueChange = { viewModel.onFieldChange(professional = it) },
                        label = { Text("Profissional/Laboratório") },
                        placeholder = { Text("Ex: Dr. Nome Sobrenome") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.location,
                        onValueChange = { viewModel.onFieldChange(location = it) },
                        label = { Text("Local") },
                        placeholder = { Text("Ex: Clínica Bem Nascer, Sala 10") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column {
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = {
                                if (it.length <= NOTES_MAX_LENGTH) {
                                    viewModel.onFieldChange(notes = it)
                                }
                            },
                            label = { Text("Anotações") },
                            placeholder = { Text("Dúvidas para perguntar, resultados...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                        )
                        Text(
                            text = "${uiState.notes.length} / $NOTES_MAX_LENGTH",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { viewModel.saveAppointment() },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    dateString: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR")) }
    val dbFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    val dateForDisplay = remember(dateString) {
        if (dateString.isNotBlank()) {
            try {
                LocalDate.parse(dateString, dbFormatter).format(displayFormatter)
            } catch (e: Exception) { "" }
        } else { "" }
    }

    Box(modifier = Modifier.clickable { showDatePicker = true }) {
        OutlinedTextField(
            value = dateForDisplay,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text("DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = "Abrir calendário")
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (dateString.isNotBlank()) {
                try {
                    LocalDate.parse(dateString, dbFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } catch (e: Exception) { Instant.now().toEpochMilli() }
            } else { Instant.now().toEpochMilli() }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // --- LINHA CORRIGIDA ---
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            onDateSelected(selectedDate.format(dbFormatter))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    label: String,
    timeString: String,
    onTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.clickable { showTimePicker = true }) {
        OutlinedTextField(
            value = timeString,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text("HH:MM") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.WatchLater, contentDescription = "Abrir seletor de hora")
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showTimePicker) {
        val time = if (timeString.isNotBlank()) {
            runCatching { LocalTime.parse(timeString) }.getOrDefault(LocalTime.now())
        } else {
            LocalTime.now()
        }
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancelar")
                        }
                        TextButton(
                            onClick = {
                                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                                onTimeSelected(selectedTime)
                                showTimePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}