package br.com.gestahub.ui.movementcounter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.Icons
import br.com.gestahub.ui.theme.Rose500
import androidx.compose.material.icons.filled.Delete
import br.com.gestahub.ui.components.ConfirmationDialog

@Composable
fun MovementCounterScreen(
    onNavigateBack: () -> Unit,
    estimatedLmp: LocalDate?,
    isDarkTheme: Boolean
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
        ConfirmationDialog(
            onDismissRequest = { sessionToDelete = null },
            onConfirm = {
                sessionToDelete?.let { viewModel.deleteSession(it) }
                sessionToDelete = null
            },
            title = "Confirmar Exclusão",
            text = "Você tem certeza que deseja apagar esta sessão?",
            confirmButtonText = "Apagar"
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                when {
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    uiState.error != null -> Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                    else -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Histórico de Sessões",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.sessions.isEmpty()) {
                                    Text(
                                        "Nenhuma sessão registrada ainda.",
                                        modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        uiState.sessions.forEach { session ->
                                            HistoryItem(
                                                session = session,
                                                lmp = estimatedLmp,
                                                isDarkTheme = isDarkTheme,
                                                onDeleteClick = { sessionToDelete = session }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    session: KickSession,
    lmp: LocalDate?,
    isDarkTheme: Boolean,
    onDeleteClick: () -> Unit
) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    color = Rose500
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "${session.kicks} movimentos",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "em ${session.durationFormatted}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Apagar Sessão"
                )
            }
        }
    }
}