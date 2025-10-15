package br.com.gestahub.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.theme.Rose500

// SEU HEADER ORIGINAL DA TELA PRINCIPAL (NÃO FOI ALTERADO)
@Composable
fun AppHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GestaHub",
                modifier = Modifier.padding(start = 12.dp),
                fontWeight = FontWeight.Bold,
                color = Rose500,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onToggle = onThemeToggle,
                    modifier = Modifier.offset(x = 8.dp)
                )

                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Ver Perfil"
                    )
                }
            }
        }
    }
}

// BOTÃO DE TEMA (NÃO FOI ALTERADO)
@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Outlined.WbSunny else Icons.Outlined.DarkMode,
            contentDescription = "Mudar tema"
        )
    }
}


// --- NOVO HEADER PARA TELAS INTERNAS ---
// ADICIONE ESTA FUNÇÃO AO FINAL DO ARQUIVO
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    title: String,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean = false
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        }
    )
}