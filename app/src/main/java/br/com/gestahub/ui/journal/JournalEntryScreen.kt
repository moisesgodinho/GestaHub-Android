// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalEntryScreen.kt
package br.com.gestahub.ui.journal

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.components.form.DatePickerField
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    val selectableDates = remember(estimatedLmp) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val selectedDate = java.time.Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(java.time.ZoneId.of("UTC")).toLocalDate()

                val isAfterToday = selectedDate.isAfter(LocalDate.now())
                val isBeforeLimit = estimatedLmp?.let {
                    val limit = it.minusMonths(2)
                    selectedDate.isBefore(limit)
                } ?: false

                return !isAfterToday && !isBeforeLimit
            }
        }
    }

    // --- CORREÇÃO APLICADA AQUI: Usando um mapa para garantir consistência ---
    val moodsMap = remember {
        linkedMapOf(
            "Feliz" to "😄 Feliz",
            "Tranquila" to "😌 Tranquila",
            "Amorosa" to "🥰 Amorosa",
            "Animada" to "🎉 Animada",
            "Cansada" to "😴 Cansada",
            "Sonolenta" to "🥱 Sonolenta",
            "Sensível" to "🥺 Sensível",
            "Ansiosa" to "😟 Ansiosa",
            "Preocupada" to "🤔 Preocupada",
            "Irritada" to "😠 Irritada",
            "Indisposta" to "🤢 Indisposta",
            "Com dores" to "😖 Com dores"
        )
    }

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

                DatePickerField(
                    label = "Data do Registro",
                    dateString = uiState.entry.date,
                    onDateSelected = { newDate ->
                        // Navega para a mesma tela com a nova data
                        onDateChange(newDate)
                    },
                    selectableDates = selectableDates
                )

                SectionTitle("Como você está se sentindo hoje?")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- LÓGICA DE SELEÇÃO CORRIGIDA ---
                    moodsMap.forEach { (saveValue, displayValue) ->
                        val isSelected = uiState.entry.mood == saveValue

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onMoodChange(saveValue) }, // Salva o valor correto (ex: "Com dores")
                            label = { Text(displayValue) } // Mostra o valor com emoji (ex: "😖 Com dores")
                        )
                    }
                }

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