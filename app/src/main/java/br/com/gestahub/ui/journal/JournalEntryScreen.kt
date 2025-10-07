// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalEntryScreen.kt
package br.com.gestahub.ui.journal

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalEntryScreen(
    estimatedLmp: LocalDate?,
    onNavigateBack: () -> Unit,
    onDateChange: (newDate: String) -> Unit,
    viewModel: JournalEntryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- LÓGICA DO SELETOR DE DATA MOVIDA PARA CÁ ---
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(uiState.entry.date)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val today = LocalDate.now()

                        // Validação 1: Data não pode ser no futuro
                        if (selectedDate.isAfter(today)) {
                            Toast.makeText(context, "A data não pode ser no futuro.", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        // Validação 2: Data não pode ser 2 meses antes da gestação
                        estimatedLmp?.let {
                            val limitDate = it.minusMonths(2)
                            if (selectedDate.isBefore(limitDate)) {
                                Toast.makeText(context, "Data muito antiga. Máximo de 2 meses antes da gestação.", Toast.LENGTH_LONG).show()
                                return@TextButton
                            }
                        }

                        // Se for válida, navega para a nova data
                        showDatePicker = false
                        onDateChange(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Emojis e Sintomas
    val moods = listOf(
        "😄 Feliz", "😌 Tranquila", "🥰 Amorosa", "🎉 Animada", "😴 Cansada",
        "🥱 Sonolenta", "🥺 Sensível", "😟 Ansiosa", "🤔 Preocupada", "😠 Irritada",
        "🤢 Indisposta", "😖 Com dores"
    )
    val commonSymptoms = listOf(
        "Azia", "Aversão a alimentos", "Câimbras", "Congestão nasal", "Constipação",
        "Desejos alimentares", "Dificuldade para dormir", "Dor de cabeça", "Dor nas costas",
        "Dor pélvica/nos ligamentos", "Fadiga", "Falta de ar", "Inchaço",
        "Mudanças de humor", "Náusea/Enjoo", "Pele com manchas/acne",
        "Sensibilidade nos seios", "Tontura", "Vômitos", "Vontade de urinar frequente"
    )

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro do Diário") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(0.dp))

                // --- NOVO CAMPO DE DATA ADICIONADO AQUI ---
                DatePickerField(
                    label = "Data do Registro",
                    dateString = uiState.entry.date,
                    onClick = { showDatePicker = true }
                )

                // Seção de Humor
                SectionTitle("Como você está se sentindo hoje?")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { mood ->
                        val moodValue = mood.split(" ").last()
                        val isSelected = uiState.entry.mood == moodValue

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onMoodChange(moodValue) },
                            label = { Text(mood) }
                        )
                    }
                }

                // Seção de Sintomas
                SectionTitle("Algum sintoma hoje?")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonSymptoms.forEach { symptom ->
                        FilterChip(
                            selected = uiState.entry.symptoms.contains(symptom),
                            onClick = { viewModel.onSymptomToggle(symptom) },
                            label = { Text(symptom) }
                        )
                    }
                }

                // Seção de Anotações
                SectionTitle("Anotações Adicionais")
                OutlinedTextField(
                    value = uiState.entry.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    label = { Text("Alguma observação importante?") },
                    placeholder = { Text("Ex: Falei com o médico, senti o bebê mexer, etc.") }
                )

                Button(
                    onClick = { viewModel.saveEntry() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Salvar Registro")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

// --- COMPONENTE REUTILIZADO DO CALCULATOR SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    dateString: String,
    onClick: () -> Unit
) {
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")) }
    val dbFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    val dateForDisplay = remember(dateString) {
        if (dateString.isNotBlank()) {
            try {
                LocalDate.parse(dateString, dbFormatter).format(displayFormatter)
            } catch (e: Exception) { "" }
        } else { "" }
    }

    Box(modifier = Modifier.clickable(onClick = onClick)) {
        OutlinedTextField(
            value = dateForDisplay,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
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
}