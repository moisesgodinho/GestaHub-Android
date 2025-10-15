package br.com.gestahub.ui.more.movementcounter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import br.com.gestahub.ui.components.Header
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MovementCounterScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: MovementCounterViewModel = viewModel(
        factory = MovementCounterViewModelFactory(
            repository = MovementCounterRepository(
                firestore = FirebaseFirestore.getInstance(),
                auth = FirebaseAuth.getInstance()
            )
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Header(
                title = "Contador de Movimentos",
                onNavigateBack = onNavigateBack,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        // --- LÓGICA DE EXIBIÇÃO ATUALIZADA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    HistoryList(sessions = uiState.sessions)
                }
            }
        }
    }
}

@Composable
fun HistoryList(sessions: List<KickSession>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Histórico de Sessões",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (sessions.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma sessão registrada ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(sessions) { session ->
                HistoryItem(session = session)
            }
        }
    }
}

@Composable
fun HistoryItem(session: KickSession) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${session.dateFormatted} às ${session.timeFormatted}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${session.kicks} movimentos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = session.durationFormatted,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}