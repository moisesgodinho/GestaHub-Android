// Local: app/src/main/java/br/com/gestahub/ui/calculator/CalculatorScreen.kt
package br.com.gestahub.ui.calculator

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions // <-- IMPORT ADICIONADO
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType // <-- IMPORT ADICIONADO
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalculatorScreen(onSaveSuccess: () -> Unit) {
    val viewModel: CalculatorViewModel = viewModel()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
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
            else -> { /* Não faz nada nos outros estados */ }
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
            0 -> DumCalculator(viewModel, saveState is SaveState.Loading)
            1 -> UltrasoundCalculator(viewModel, saveState is SaveState.Loading)
        }
    }
}

@Composable
fun DumCalculator(viewModel: CalculatorViewModel, isLoading: Boolean) {
    var lmp by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = lmp,
            onValueChange = { lmp = it },
            label = { Text("Data da Última Menstruação (DUM)") },
            placeholder = { Text("AAAA-MM-DD") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { viewModel.saveLmp(lmp) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Salvar")
        }
    }
}

@Composable
fun UltrasoundCalculator(viewModel: CalculatorViewModel, isLoading: Boolean) {
    var examDate by remember { mutableStateOf("") }
    var weeks by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }

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
        Button(
            onClick = { viewModel.saveUltrasound(examDate, weeks, days) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Salvar")
        }
    }
}