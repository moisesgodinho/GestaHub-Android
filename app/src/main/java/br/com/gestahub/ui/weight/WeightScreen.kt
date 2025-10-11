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
    onNavigateToProfileForm: () -> Unit,
    viewModel: WeightViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    val isProfileFilled = profile != null && profile.height > 0 && profile.prePregnancyWeight > 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (!isProfileFilled) {
                InitialProfilePrompt(onNavigateToProfileForm)
            } else {
                ProfileCard(
                    profile = profile!!,
                    isDarkTheme = isDarkTheme, // Passa a informação de tema
                    onEditClick = onNavigateToProfileForm
                )
            }
        }

        if (isProfileFilled) {
            item {
                Text(
                    text = "Histórico de Peso",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.entries.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
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
}


@Composable
fun ProfileCard(profile: WeightProfile, isDarkTheme: Boolean, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Seus Dados Iniciais", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onEditClick) {
                    Text("Alterar dados")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // --- CORREÇÃO APLICADA AQUI ---
            // A Row agora contém os novos InfoCard individuais.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    InfoCard(
                        label = "Altura",
                        value = "${profile.height} cm",
                        isDarkTheme = isDarkTheme
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    InfoCard(
                        label = "Peso Inicial",
                        value = "${profile.prePregnancyWeight} kg",
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

/**
 * Novo componente de card de informação, replicado da HomeScreen para consistência.
 */
@Composable
fun InfoCard(
    label: String,
    value: String,
    isDarkTheme: Boolean
) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall, // Ajustado para 'Small' para melhor encaixe
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun InitialProfilePrompt(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Vamos começar!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Para calcular seu IMC e acompanhar seu ganho de peso, precisamos da sua altura e peso antes da gestação.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Text("Adicionar Meus Dados")
            }
        }
    }
}

// O InfoColumn não é mais necessário, foi substituído pelo InfoCard.

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
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            text = "Toque no botão '+' para adicionar seu primeiro registro.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}