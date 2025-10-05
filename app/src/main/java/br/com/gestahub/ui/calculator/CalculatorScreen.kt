package br.com.gestahub.ui.calculator

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onSaveSuccess: () -> Unit,
    onCancelClick: () -> Unit,
    initialLmp: String?,
    initialExamDate: String?,
    initialWeeks: String?,
    initialDays: String?
) {
    val viewModel: CalculatorViewModel = viewModel()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current

    val initialTab = if (!initialExamDate.isNullOrEmpty()) 1 else 0
    var selectedTab by remember { mutableStateOf(initialTab) }
    val tabs = listOf("Calculadora DUM", "Calculadora Ultrassom")

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                Toast.makeText(context, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
                onSaveSuccess()
                viewModel.resetState()
            }
            is SaveState.Error -> {
                val message = (saveState as SaveState.Error).message
                Toast.makeText(context, "Erro: $message", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Calculadoras Gestacionais") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> DumCalculator(viewModel, saveState is SaveState.Loading, initialLmp, onCancelClick)
                1 -> UltrasoundCalculator(viewModel, saveState is SaveState.Loading, initialExamDate, initialWeeks, initialDays, onCancelClick)
            }
        }
    }
}

@Composable
fun DumCalculator(
    viewModel: CalculatorViewModel,
    isLoading: Boolean,
    initialLmp: String?,
    onCancelClick: () -> Unit
) {
    var lmp by remember { mutableStateOf(initialLmp ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- COMPONENTE DE DATA APLICADO AQUI ---
        DatePickerField(
            label = "Data da Última Menstruação (DUM)",
            dateString = lmp,
            onDateSelected = { lmp = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancelClick, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = { viewModel.saveLmp(lmp) },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Salvar")
            }
        }
    }
}

@Composable
fun UltrasoundCalculator(
    viewModel: CalculatorViewModel,
    isLoading: Boolean,
    initialExamDate: String?,
    initialWeeks: String?,
    initialDays: String?,
    onCancelClick: () -> Unit
) {
    var examDate by remember { mutableStateOf(initialExamDate ?: "") }
    var weeks by remember { mutableStateOf(initialWeeks ?: "") }
    var days by remember { mutableStateOf(initialDays ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // --- COMPONENTE DE DATA APLICADO AQUI ---
            DatePickerField(
                label = "Data do Ultrassom",
                dateString = examDate,
                onDateSelected = { examDate = it }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weeks,
                    onValueChange = { weeks = it },
                    label = { Text("Semanas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Dias") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancelClick, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = { viewModel.saveUltrasound(examDate, weeks, days) },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Salvar")
            }
        }
    }
}


// --- COMPONENTE REUTILIZÁVEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    dateString: String, // Data no formato AAAA-MM-DD
    onDateSelected: (String) -> Unit // Retorna a data no formato AAAA-MM-DD
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR")) }
    val dbFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("pt", "BR")) }

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
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
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