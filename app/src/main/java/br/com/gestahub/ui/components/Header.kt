// app/src/main/java/br/com/gestahub/ui/components/Header.kt
package br.com.gestahub.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import br.com.gestahub.ui.theme.Rose500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    TopAppBar(
        title = { Text("GestaHub", color = Rose500) },
        actions = {
            IconButton(onClick = onThemeToggle) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.Nightlight,
                    contentDescription = "Mudar tema"
                )
            }
        }
    )
}

annotation class Header
