package br.com.gestahub.ui.movementcounter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.components.ConfirmationDialog
import br.com.gestahub.ui.components.Header
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import br.com.gestahub.ui.theme.Rose500
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

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
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    val onBackPress: () -> Unit = {
        if (uiState.isSessionActive) {
            showExitConfirmationDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Intercepta o botão "voltar" do sistema Android
    BackHandler(enabled = uiState.isSessionActive) {
        onBackPress()
    }

    // Diálogo de confirmação para sair da sessão
    if (showExitConfirmationDialog) {
        ConfirmationDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            onConfirm = {
                viewModel.discardSession()
                showExitConfirmationDialog = false
                onNavigateBack()
            },
            title = "Sair da Sessão?",
            text = "Você tem certeza que deseja sair? A contagem atual será perdida.",
            confirmButtonText = "Sair",
            dismissButtonText = "Cancelar"
        )
    }

    // Diálogo para apagar um item do histórico
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
                onNavigateBack = onBackPress, // Usa a nova lógica de voltar
                showBackButton = true
            )
        }
    ) { innerPadding ->
        if (uiState.isSessionActive) {
            ActiveSessionScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                onIncrementClick = { viewModel.incrementKickCount() },
                onStopClick = { viewModel.stopSession() }
            )
        } else {
            InitialScreenLayout(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                estimatedLmp = estimatedLmp,
                isDarkTheme = isDarkTheme,
                onStartClick = { viewModel.startSession() },
                onDeleteClick = { sessionToDelete = it }
            )
        }
    }
}

@Composable
fun InitialScreenLayout(
    modifier: Modifier = Modifier,
    uiState: MovementCounterUiState,
    estimatedLmp: LocalDate?,
    isDarkTheme: Boolean,
    onStartClick: () -> Unit,
    onDeleteClick: (KickSession) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MovementTrackerCard(onStartClick = onStartClick)
        }
        item {
            WhyCountMovementsCard()
        }
        item {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                uiState.error != null -> Text(
                    text = uiState.error!!,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
                else -> {
                    HistoryCard(
                        sessions = uiState.sessions,
                        lmp = estimatedLmp,
                        isDarkTheme = isDarkTheme,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}

// --- CARD INFORMATIVO COM A CORREÇÃO FINAL ---
@Composable
fun WhyCountMovementsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            // A Borda Azul
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary) // Cor azul do tema
            )

            // Conteúdo
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Por que contar os movimentos?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Esta é uma ferramenta importante para monitorar o bem-estar do seu bebê, especialmente no terceiro trimestre (a partir da 28ª semana). Movimentos fetais regulares são um forte sinal de que o bebê está saudável. Uma mudança no padrão pode ser um sinal de alerta precoce, permitindo que você e seu médico ajam rapidamente.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Como fazer a contagem?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Escolha um horário: ")
                        }
                        append("Dê preferência a um momento em que seu bebê costuma estar mais ativo (geralmente à noite ou após uma refeição).")
                    }
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Fique confortável: ")
                        }
                        append("Deite-se de lado (preferencialmente o esquerdo, para melhorar a circulação) ou sente-se com os pés para cima.")
                    }
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Conte os movimentos: ")
                        }
                        append("Inicie a sessão e registre cada movimento que sentir (chutes, giros, vibrações). O objetivo comum é sentir ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("10 movimentos em até 2 horas.")
                        }
                    }
                )
                Text(
                    text = "Quando devo procurar meu médico?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = buildAnnotatedString {
                        append("O mais importante é conhecer o padrão ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("normal ")
                        }
                        append("do seu bebê. Se você notar uma diminuição significativa e prolongada na atividade dele, ou se o tempo para atingir 10 movimentos aumentar muito, entre em contato com seu obstetra. ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Confie no seu instinto.")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ActiveSessionScreen(
    modifier: Modifier = Modifier,
    uiState: MovementCounterUiState,
    onIncrementClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${uiState.kickCount}",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (uiState.kickCount == 1) "movimento" else "movimentos",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = formatElapsedTime(uiState.elapsedTimeInSeconds),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onIncrementClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Movimentou!", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onStopClick) {
                Text("Finalizar e Salvar Sessão")
            }
        }
    }
}

@Composable
fun MovementTrackerCard(
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Monitore o padrão de movimentos do seu bebê. Pressione \"Iniciar\" e, a cada movimento (chute, giro ou vibração), clique em \"Movimentou!\".",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onStartClick) {
                Text("Iniciar Sessão")
            }
        }
    }
}

@Composable
fun HistoryCard(
    sessions: List<KickSession>,
    lmp: LocalDate?,
    isDarkTheme: Boolean,
    onDeleteClick: (KickSession) -> Unit
) {
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

            if (sessions.isEmpty()) {
                Text(
                    "Nenhuma sessão registrada ainda.",
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sessions.forEach { session ->
                        HistoryItem(
                            session = session,
                            lmp = lmp,
                            isDarkTheme = isDarkTheme,
                            onDeleteClick = { onDeleteClick(session) }
                        )
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

private fun formatElapsedTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}