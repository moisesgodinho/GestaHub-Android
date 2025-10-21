package br.com.gestahub.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WaterDrop
// V ADICIONE ESTE IMPORT V
import androidx.compose.material.icons.filled.Vaccines
// ^ ADICIONE ESTE IMPORT ^
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Helper data class simplificada, sem o painter
private data class FeatureCardInfo(
    val onClick: () -> Unit,
    val icon: ImageVector,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToMovementCounter: () -> Unit,
    onNavigateToMaternityBag: () -> Unit,
    onNavigateToHydrationTracker: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToContractionTimer: () -> Unit,
    // V ADICIONE O PARÂMETRO ABAIXO V
    onNavigateToMedicationTracker: () -> Unit
) {
    val features = listOf(
        FeatureCardInfo(onNavigateToMovementCounter, Icons.Default.TouchApp, "Contador de Movimentos"),
        FeatureCardInfo(onNavigateToContractionTimer, Icons.Default.Timer, "Cronômetro de Contrações"),
        // V ADICIONE O ITEM ABAIXO V
        FeatureCardInfo(onNavigateToMedicationTracker, Icons.Default.Vaccines, "Controle de Medicamentos"),
        // ^ ADICIONE O ITEM ACIMA ^
        FeatureCardInfo(onNavigateToMaternityBag, Icons.Default.BusinessCenter, "Mala Maternidade"),
        FeatureCardInfo(onNavigateToHydrationTracker, Icons.Default.WaterDrop, "Controle de Hidratação"),
        FeatureCardInfo(onNavigateToShoppingList, Icons.Default.ShoppingCart, "Lista de Compras")
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(features) { feature ->
            MoreFunctionCard(
                onClick = feature.onClick,
                icon = feature.icon,
                text = feature.text
            )
        }
    }
}

// MoreFunctionCard simplificado, sem o painter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreFunctionCard(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(130.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}