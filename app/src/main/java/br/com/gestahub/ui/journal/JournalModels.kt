// Local: app/src/main/java/br/com/gestahub/ui/journal/JournalModels.kt
package br.com.gestahub.ui.journal

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class JournalEntry(
    @DocumentId val id: String = "",
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val mood: String = "",
    val symptoms: List<String> = emptyList(),
    val notes: String = ""
)