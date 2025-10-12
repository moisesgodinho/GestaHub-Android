package br.com.gestahub.ui.calculator

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
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

    val initialTab = if (!initialExamDate.isNullOrEmpty() && initialExamDate != "null") 1 else 0
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
            TopAppBar(
                title = { Text("Calculadoras Gestacionais") },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
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

            InfoSection()

            when (selectedTab) {
                0 -> DumCalculator(viewModel, saveState is SaveState.Loading, initialLmp, onCancelClick)
                1 -> UltrasoundCalculator(viewModel, saveState is SaveState.Loading, initialExamDate, initialWeeks, initialDays, onCancelClick)
            }
        }
    }
}

@Composable
fun InfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "O cálculo pela data do ultrassom (especialmente o do 1º trimestre) é considerado mais preciso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = buildAnnotatedString {
                append("Para a DUM, utilize sempre o ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("primeiro dia")
                }
                append(" do seu último ciclo menstrual.")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DumCalculator(
    viewModel: CalculatorViewModel,
    isLoading: Boolean,
    initialLmp: String?,
    onCancelClick: () -> Unit
) {
    var lmp by remember { mutableStateOf(initialLmp?.takeIf { it != "null" } ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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
    var examDate by remember { mutableStateOf(initialExamDate?.takeIf { it != "null" } ?: "") }
    var weeks by remember { mutableStateOf(initialWeeks?.takeIf { it != "null" } ?: "") }
    var days by remember { mutableStateOf(initialDays?.takeIf { it != "null" } ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    dateString: String,
    onDateSelected: (String) -> Unit
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
                            // --- CORREÇÃO APLICADA AQUI ---
                            // Forçamos a conversão da data para o fuso UTC, ignorando o fuso do celular.
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