// app/src/main/java/br/com/gestahub/ui/home/components/HydrationSummaryCard.kt
package br.com.gestahub.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.hydration.WaterIntakeEntry
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationSummaryCard(
    hydrationData: WaterIntakeEntry?,
    onAddWater: () -> Unit,
    onUndoWater: () -> Unit,
    onNavigateToTracker: () -> Unit
) {
    if (hydrationData == null) return

    val haptic = LocalHapticFeedback.current

    val animatedProgress by animateFloatAsState(
        targetValue = if (hydrationData.goal > 0) min(hydrationData.current.toFloat() / hydrationData.goal.toFloat(), 1f) else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "HydrationProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onNavigateToTracker
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Hidratação de Hoje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToTracker) {
                    Text("Ver mais")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${hydrationData.current} / ${hydrationData.goal} ml",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = Color(0xFF64B5F6),
                        trackColor = Color(0xFFBBDEFB)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- BOTÃO DE REMOVER ATUALIZADO ---
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUndoWater()
                        },
                        modifier = Modifier.size(40.dp), // Altura e largura definidas
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        enabled = hydrationData.history.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary, // Cor alterada
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Desfazer última adição")
                    }

                    // --- BOTÃO DE ADICIONAR ATUALIZADO ---
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddWater()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp) // Altura definida
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar um copo (${hydrationData.cupSize} ml)")
                    }
                }
            }
        }
    }
}