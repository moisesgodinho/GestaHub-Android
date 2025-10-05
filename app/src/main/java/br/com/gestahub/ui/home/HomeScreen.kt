package br.com.gestahub.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onAddDataClick: () -> Unit, // Renomeado para clareza
    onEditDataClick: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val dataState = uiState.dataState

    // --- LÓGICA DE EXIBIÇÃO CORRIGIDA ---
    // A tela agora gerencia seus próprios estados internos
    when (dataState) {
        is GestationalDataState.Loading -> {
            // Mostra um spinner enquanto os dados são carregados
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is GestationalDataState.HasData -> {
            // Mostra o dashboard principal quando há dados
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GestationalInfoDashboard(dataState, onEditDataClick)
            }
        }
        is GestationalDataState.NoData -> {
            // Mostra a tela de "estado vazio" se não houver dados
            EmptyHomeScreen(onAddDataClick)
        }
    }
}

@Composable
fun EmptyHomeScreen(onAddDataClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhum dado encontrado",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Para começar, adicione a data da sua última menstruação ou os dados do seu último ultrassom.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddDataClick) {
            Text("Adicionar Dados da Gestação")
        }
    }
}


@Composable
fun GestationalInfoDashboard(state: GestationalDataState.HasData, onEditDataClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sua Gestação",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onEditDataClick) {
                    Text("Alterar Dados")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            InfoCard("Idade Gestacional", "${state.gestationalWeeks}s ${state.gestationalDays}d")
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard("Data Provável do Parto", state.dueDate)
            Spacer(modifier = Modifier.height(16.dp))

            CountdownCard(weeks = state.countdownWeeks, days = state.countdownDays)
        }
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CountdownCard(weeks: Int, days: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF472B6), Color(0xFFFB923C))
                ),
                shape = MaterialTheme.shapes.large
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (weeks >= 0 && days >= 0) {
                val text = if (weeks == 0 && days == 0) "Hoje é o grande dia!" else "${weeks}s ${days}d"
                val subtext = if (weeks == 0 && days == 0) "❤️" else "para o grande dia!"

                Text(
                    text = text,
                    fontSize = if(text.length > 15) 24.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtext,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            } else {
                Text(
                    text = "Bem-vindo(a) ao mundo! ❤️",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}