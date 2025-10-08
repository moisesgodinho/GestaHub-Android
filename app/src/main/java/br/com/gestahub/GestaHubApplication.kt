// Local: app/src/main/java/br/com/gestahub/GestaHubApplication.kt
package br.com.gestahub

import android.app.Application
import androidx.emoji2.text.EmojiCompat

class GestaHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa a biblioteca de emojis para todo o aplicativo
        EmojiCompat.init(this)
    }
}