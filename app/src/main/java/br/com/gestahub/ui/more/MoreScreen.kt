package br.com.gestahub.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.ShoppingCart // 1. Importe o ícone novo
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToMovementCounter: () -> Unit,
    onNavigateToMaternityBag: () -> Unit,
    onNavigateToHydrationTracker: () -> Unit,
    onNavigateToShoppingList: () -> Unit // 2. Adicione o novo callback
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Adiciona espaço entre as linhas
    ) {
        // Primeira linha de cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MoreFunctionCard(
                onClick = onNavigateToMovementCounter,
                icon = Icons.Default.TouchApp,
                text = "Contador de Movimentos"
            )
            MoreFunctionCard(
                onClick = onNavigateToMaternityBag,
                icon = Icons.Default.BusinessCenter,
                text = "Mala Maternidade"
            )
        }
        // Segunda linha de cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MoreFunctionCard(
                onClick = onNavigateToHydrationTracker,
                icon = Icons.Default.WaterDrop,
                text = "Controle de Hidratação"
            )
            // 3. Adicione o novo card aqui
            MoreFunctionCard(
                onClick = onNavigateToShoppingList,
                icon = Icons.Default.ShoppingCart,
                text = "Lista de Compras"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreFunctionCard(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(120.dp), // Você pode ajustar o tamanho se preferir
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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