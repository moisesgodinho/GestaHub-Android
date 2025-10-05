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

// Paleta de Cores para o Tema Claro (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,
    secondary = Green600,
    onSecondary = White,
    background = Slate100,
    onBackground = Slate900,
    surface = White,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate700,
    surfaceTint = White
)

// Paleta de Cores para o Tema Escuro (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = Indigo400,
    onPrimary = Indigo900,
    // --- CORREÇÃO APLICADA AQUI ---
    // Usando a nova cor, mais suave
    primaryContainer = DarkBlueContainer,
    onPrimaryContainer = Indigo100,
    secondary = Green400,
    onSecondary = Slate900,
    background = Slate900,
    onBackground = Slate200,
    surface = Slate800,
    onSurface = Slate200,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,
)

@Composable
fun GestaHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            WindowCompat.setDecorFitsSystemWindows(window, false)

            val isLight = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}