// app/src/main/java/br/com/gestahub/ui/main/MainViewModel.kt
package br.com.gestahub.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestahub.data.ThemeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val themeManager = ThemeManager(application)

    // Expõe o estado do tema para a UI
    val isDarkTheme = themeManager.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Função que a UI vai chamar para trocar o tema
    fun toggleTheme() {
        viewModelScope.launch {
            val newThemeValue = !isDarkTheme.value
            themeManager.setDarkTheme(newThemeValue)
        }
    }
}