package br.com.gestahub.ui.weight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightScreen(
    contentPadding: PaddingValues,
    isDarkTheme: Boolean,
    viewModel: WeightViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- CORREÇÃO APLICADA AQUI ---
    // A Scaffold foi removida completamente.
    // O layout principal agora é um Box.

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.entries.isEmpty()) {
        EmptyState(contentPadding = contentPadding)
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding), // O espaçamento da navegação é aplicado aqui
            contentPadding = PaddingValues(horizontal = 16.dp), // Apenas espaçamento horizontal interno
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Histórico de Peso",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(uiState.entries, key = { it.date }) { entry ->
                WeightItem(
                    entry = entry,
                    isDarkTheme = isDarkTheme,
                    onDelete = { viewModel.deleteWeightEntry(entry) }
                )
            }
        }
    }
}

@Composable
fun WeightItem(entry: WeightEntry, isDarkTheme: Boolean, onDelete: () -> Unit) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${String.format("%.1f", entry.weight)} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.date.let {
                        try {
                            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            formatter.format(parser.parse(it) ?: Date())
                        } catch (e: Exception) {
                            it
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "IMC: ${String.format("%.1f", entry.bmi)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir peso")
            }
        }
    }
}

@Composable
fun EmptyState(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhum registro de peso",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque no botão '+' para adicionar seu primeiro registro de peso.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}