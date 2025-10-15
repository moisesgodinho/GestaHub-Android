package br.com.gestahub.ui.more.movementcounter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.components.Header
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

@Composable
fun MovementCounterScreen(
    onNavigateBack: () -> Unit,
    // Precisamos da DUM (LMP) para calcular a idade gestacional
    estimatedLmp: LocalDate?
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
    var sessionToDelete by remember { mutableStateOf<KickSession?>(null) }

    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Você tem certeza que deseja apagar esta sessão?") },
            confirmButton = {
                Button(
                    onClick = {
                        sessionToDelete?.let { viewModel.deleteSession(it) }
                        sessionToDelete = null
                    }
                ) {
                    Text("Apagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Header(
                title = "Contador de Movimentos",
                onNavigateBack = onNavigateBack,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text(
                    text = uiState.error!!,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
                else -> HistoryList(
                    sessions = uiState.sessions,
                    lmp = estimatedLmp,
                    onDeleteRequest = { sessionToDelete = it }
                )
            }
        }
    }
}

@Composable
fun HistoryList(
    sessions: List<KickSession>,
    lmp: LocalDate?,
    onDeleteRequest: (KickSession) -> Unit
) {
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
            item { Text("Nenhuma sessão registrada ainda.") }
        } else {
            items(sessions) { session ->
                HistoryItem(
                    session = session,
                    lmp = lmp,
                    onDeleteClick = { onDeleteRequest(session) }
                )
            }
        }
    }
}

@Composable
fun HistoryItem(
    session: KickSession,
    lmp: LocalDate?,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coluna da Esquerda
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.dateFormatted,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.startTimeFormatted} - ${session.endTimeFormatted}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = session.getGestationalAge(lmp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Coluna da Direita
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = session.kicks.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "em ${session.durationFormatted}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botão de Deletar
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Apagar Sessão",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}