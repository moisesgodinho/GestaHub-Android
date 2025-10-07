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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: JournalEntryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val entryDate = LocalDate.parse(uiState.entry.date)
    val formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))

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
                title = { Text(entryDate.format(formatter)) },
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
                    .verticalScroll(rememberScrollState())
            ) {
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

                Spacer(Modifier.height(24.dp))

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

                Spacer(Modifier.height(24.dp))

                // Seção de Anotações
                SectionTitle("Anotações Adicionais")
                OutlinedTextField(
                    value = uiState.entry.notes,
                    // --- CORREÇÃO APLICADA AQUI ---
                    onValueChange = { viewModel.onNotesChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    label = { Text("Alguma observação importante?") },
                    placeholder = { Text("Ex: Falei com o médico, senti o bebê mexer, etc.") }
                )

                Spacer(Modifier.height(24.dp))

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