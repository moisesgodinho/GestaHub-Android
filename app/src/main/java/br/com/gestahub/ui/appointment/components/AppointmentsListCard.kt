// Local: app/src/main/java/br/com/gestahub/ui/appointment/components/AppointmentsListCard.kt
package br.com.gestahub.ui.appointment.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.appointment.Appointment
import java.time.LocalDate

@Composable
fun AppointmentsListCard(
    title: String,
    appointments: List<Appointment>,
    lmpDate: LocalDate?,
    isDarkTheme: Boolean,
    onToggleDone: (Appointment) -> Unit,
    onEditClick: (Appointment) -> Unit,
    onDeleteOrClearRequest: (Appointment) -> Unit
) {
    var itemsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        itemsVisible = true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                appointments.forEachIndexed { index, appointment ->
                    AnimatedVisibility(
                        visible = itemsVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = index * 100))
                                + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(durationMillis = 500, delayMillis = index * 100)
                        )
                    ) {
                        AppointmentItem(
                            appointment = appointment,
                            lmpDate = lmpDate,
                            isDarkTheme = isDarkTheme,
                            onToggleDone = onToggleDone,
                            onEdit = onEditClick,
                            onDelete = onDeleteOrClearRequest
                        )
                    }
                }
            }
        }
    }
}