package br.com.gestahub.ui.more.movementcounter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.gestahub.ui.components.Header // Este import agora está correto

@Composable
fun MovementCounterScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Header( // E esta chamada agora vai funcionar
                title = "Contador de Movimentos",
                onNavigateBack = onNavigateBack,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Conteúdo do contador de movimentos")
        }
    }
}