// Inserir em: app/src/main/java/br/com/gestahub/ui/theme/Theme.kt
package br.com.gestahub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta de cores para o Tema Escuro
private val DarkColorScheme = darkColorScheme(
    primary = Rose500, // A cor principal se destaca bem
    background = Slate800, // Fundo escuro
    surface = Slate800, // Superfície dos cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E8F0), // Texto principal (slate-200)
    onSurface = Color(0xFFE2E8F0), // Texto nos cards (slate-200)
    onSurfaceVariant = Color(0xFF94A3B8) // Texto secundário (slate-400)
)

// Paleta de cores para o Tema Claro
private val LightColorScheme = lightColorScheme(
    primary = Rose500,
    background = Color(0xFFF8FAFC), // Fundo claro (slate-50)
    surface = Color.White, // Superfície dos cards
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF0F172A), // Texto principal (slate-900)
    onSurface = Color(0xFF0F172A), // Texto nos cards (slate-900)
    onSurfaceVariant = Color(0xFF64748B) // Texto secundário (slate-500)
)

@Composable
fun GestaHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}