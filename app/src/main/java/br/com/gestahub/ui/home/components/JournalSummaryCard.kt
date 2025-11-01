// app/src/main/java/br/com/gestahub/ui/home/components/JournalSummaryCard.kt
package br.com.gestahub.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.journal.JournalEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalSummaryCard(
    entry: JournalEntry?,
    onRegisterClick: () -> Unit,
    onEditClick: () -> Unit,
    onNavigateToJournal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // --- ALTERAÇÃO APLICADA AQUI ---
        // O Card principal não tem mais uma ação de clique.
        // As ações são tratadas apenas pelos botões internos.
        onClick = { /* Card principal não é clicável */ }
    ) {
        if (entry == null) {
            // Estado 1: Lembrete (Ação apenas no botão)
            ReminderJournalContent(onRegisterClick = onRegisterClick)
        } else {
            // Estado 2: Resumo (Ações nos botões "Ver mais" e "Editar")
            SummaryJournalContent(
                entry = entry,
                onEditClick = onEditClick,
                onNavigateToJournal = onNavigateToJournal
            )
        }
    }
}

@Composable
private fun ReminderJournalContent(onRegisterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Book,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Como você está se sentindo hoje?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Reserve um momento para registrar seu humor e sintomas. Cuidar de você é fundamental.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        // A única ação clicável neste estado é o botão.
        Button(onClick = onRegisterClick) {
            Text("Registrar Humor")
        }
    }
}

@Composable
private fun SummaryJournalContent(
    entry: JournalEntry,
    onEditClick: () -> Unit,
    onNavigateToJournal: () -> Unit
) {
    val moodsMap = mapOf(
        "Feliz" to "😄 Feliz", "Tranquila" to "😌 Tranquila", "Amorosa" to "🥰 Amorosa",
        "Animada" to "🎉 Animada", "Cansada" to "😴 Cansada", "Sonolenta" to "🥱 Sonolenta",
        "Sensível" to "🥺 Sensível", "Ansiosa" to "😟 Ansiosa", "Preocupada" to "🤔 Preocupada",
        "Irritada" to "😠 Irritada", "Indisposta" to "🤢 Indisposta", "Com dores" to "😖 Com dores"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Linha superior com "Humor de Hoje" e "Ver mais"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Humor de Hoje",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Ação 1: "Ver mais"
            TextButton(
                onClick = onNavigateToJournal,
                modifier = Modifier.offset(x = 8.dp)
            ) {
                Text("Ver mais")
            }
        }

        // Linha inferior com o Humor e o botão Editar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = moodsMap[entry.mood] ?: entry.mood.ifBlank { "Não registrado" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            // Ação 2: "Editar"
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar registro",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}