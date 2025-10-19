package br.com.gestahub.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Helper data class para manter as informações dos cards organizadas.
private data class FeatureCardInfo(
    val onClick: () -> Unit,
    val icon: ImageVector,
    val text: String
)

// CORREÇÃO APLICADA AQUI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToMovementCounter: () -> Unit,
    onNavigateToMaternityBag: () -> Unit,
    onNavigateToHydrationTracker: () -> Unit,
    onNavigateToShoppingList: () -> Unit
) {
    // Criar uma lista com todos os cards facilita a manutenção.
    val features = listOf(
        FeatureCardInfo(onNavigateToMovementCounter, Icons.Default.TouchApp, "Contador de Movimentos"),
        FeatureCardInfo(onNavigateToMaternityBag, Icons.Default.BusinessCenter, "Mala Maternidade"),
        FeatureCardInfo(onNavigateToHydrationTracker, Icons.Default.WaterDrop, "Controle de Hidratação"),
        FeatureCardInfo(onNavigateToShoppingList, Icons.Default.ShoppingCart, "Lista de Compras")
        // Adicione novos cards aqui no futuro.
    )

    val itemsPerRow = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        features.chunked(itemsPerRow).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { feature ->
                    MoreFunctionCard(
                        onClick = feature.onClick,
                        icon = feature.icon,
                        text = feature.text
                    )
                }

                val emptySpaces = itemsPerRow - rowItems.size
                repeat(emptySpaces) {
                    Spacer(modifier = Modifier.size(width = 110.dp, height = 120.dp))
                }
            }
        }
    }
}

// CORREÇÃO APLICADA AQUI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreFunctionCard(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 110.dp, height = 120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}