// Local: app/src/main/java/br/com/gestahub/ui/calculator/CalculatorScreen.kt
package br.com.gestahub.ui.calculator

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalculatorScreen(
    onSaveSuccess: () -> Unit,
    onCancelClick: () -> Unit, // <-- NOVO PARÂMETRO
    initialLmp: String?,
    initialExamDate: String?,
    initialWeeks: String?,
    initialDays: String?
) {
    val viewModel: CalculatorViewModel = viewModel()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current

    val initialTab = if (!initialExamDate.isNullOrEmpty()) 1 else 0
    var selectedTab by remember { mutableIntStateOf(initialTab) }
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

    Column(modifier = Modifier.fillMaxSize()) {
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

@Composable
fun DumCalculator(
    viewModel: CalculatorViewModel,
    isLoading: Boolean,
    initialLmp: String?,
    onCancelClick: () -> Unit // <-- NOVO PARÂMETRO
) {
    var lmp by remember { mutableStateOf(initialLmp ?: "") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = lmp,
            onValueChange = { lmp = it },
            label = { Text("Data da Última Menstruação (DUM)") },
            placeholder = { Text("AAAA-MM-DD") },
            modifier = Modifier.fillMaxWidth()
        )
        // Linha para os botões
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            ) {
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
    onCancelClick: () -> Unit // <-- NOVO PARÂMETRO
) {
    var examDate by remember { mutableStateOf(initialExamDate ?: "") }
    var weeks by remember { mutableStateOf(initialWeeks ?: "") }
    var days by remember { mutableStateOf(initialDays ?: "") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = examDate,
            onValueChange = { examDate = it },
            label = { Text("Data do Ultrassom") },
            placeholder = { Text("AAAA-MM-DD") },
            modifier = Modifier.fillMaxWidth()
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
        // Linha para os botões
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            ) {
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