package br.com.gestahub.ui.journal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import br.com.gestahub.ui.theme.Rose500


@Composable
fun ViewJournalEntryDialog(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    onEdit: (JournalEntry) -> Unit,
    onDelete: (JournalEntry) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            DialogTitle(date = LocalDate.parse(entry.date), isTitle = true, highlight = true)
        },
        text = {
            JournalItemContent(entry = entry)
        },
        confirmButton = {
            Row {
                IconButton(onClick = { onDelete(entry) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                }
                IconButton(onClick = { onEdit(entry) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun NewJournalEntryDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            DialogTitle(date = date, isTitle = true, highlight = true)
        },
        text = {
            Text("Nenhum registo encontrado para este dia. Gostaria de adicionar um novo?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Registo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogTitle(date: LocalDate, isTitle: Boolean = false, highlight: Boolean = false) {
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale("pt", "BR"))
    val restOfDateFormatter = if (isTitle) {
        DateTimeFormatter.ofPattern(", dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
    } else {
        DateTimeFormatter.ofPattern(", dd", Locale("pt", "BR"))
    }

    val dayOfWeek = date.format(dayOfWeekFormatter).lowercase(Locale.getDefault())
    val restOfDate = date.format(restOfDateFormatter)

    val textPrefix = if (isTitle) "Registo de " else ""
    val titleStyle = if (isTitle) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium

    Text(
        text = buildAnnotatedString {
            append(textPrefix)
            if (highlight) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Rose500)) {
                    append(dayOfWeek)
                    append(restOfDate)
                }
            } else {
                append(dayOfWeek)
                append(restOfDate)
            }
        },
        style = titleStyle
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalItemContent(entry: JournalEntry) {
    // --- CORREÇÃO APLICADA AQUI ---
    // O mapa agora usa a chave de texto ("Com dores") para encontrar
    // o valor completo com emoji ("😖 Com dores").
    val moodsMap = mapOf(
        "Feliz" to "😄 Feliz",
        "Tranquila" to "😌 Tranquila",
        "Amorosa" to "🥰 Amorosa",
        "Animada" to "🎉 Animada",
        "Cansada" to "😴 Cansada",
        "Sonolenta" to "🥱 Sonolenta",
        "Sensível" to "🥺 Sensível",
        "Ansiosa" to "😟 Ansiosa",
        "Preocupada" to "🤔 Preocupada",
        "Irritada" to "😠 Irritada",
        "Indisposta" to "🤢 Indisposta",
        "Com dores" to "😖 Com dores"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (entry.mood.isNotBlank()) {
            val moodLabel = moodsMap[entry.mood] ?: entry.mood
            Text(
                text = moodLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (entry.symptoms.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sintomas:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.symptoms.forEach { symptom ->
                        SuggestionChip(
                            onClick = { /* Apenas para exibição */ },
                            label = { Text(symptom) },
                            border = null,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }

        if (entry.notes.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Anotações:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}