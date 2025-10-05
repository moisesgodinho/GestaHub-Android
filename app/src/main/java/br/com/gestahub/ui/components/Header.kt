package br.com.gestahub.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import br.com.gestahub.ui.theme.Rose500 // Importa a nova cor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "GestaHub",
                fontWeight = FontWeight.Bold,
                color = Rose500 // <-- COR ALTERADA AQUI
            )
        },
        actions = {
            ThemeToggleButton(isDarkTheme = isDarkTheme, onToggle = onThemeToggle)
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Ver Perfil"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ThemeToggleButton(isDarkTheme: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
            contentDescription = "Mudar tema"
        )
    }
}