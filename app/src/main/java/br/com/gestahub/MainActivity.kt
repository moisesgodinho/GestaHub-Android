package br.com.gestahub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.login.AuthState
import br.com.gestahub.ui.login.AuthScreen
import br.com.gestahub.ui.login.AuthViewModel
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.navigation.GestaHubApp
import br.com.gestahub.ui.theme.GestaHubTheme
import dagger.hilt.android.AndroidEntryPoint // <-- ADICIONE ESTE IMPORT
import br.com.gestahub.workers.NotificationWorker // <-- CORRIGIR ESTA LINHA

@AndroidEntryPoint // <-- ADICIONE ESTA ANOTAÇÃO
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permissão concedida, você pode querer fazer algo aqui
            } else {
                // Permissão negada, você pode mostrar uma mensagem para o usuário
            }
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
            val authState by authViewModel.authState.collectAsState()

            GestaHubTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val state = authState) {
                        is AuthState.Success -> {
                            // Solicita a permissão de notificação quando o usuário está logado
                            LaunchedEffect(Unit) {
                                askNotificationPermission()
                            }
                            GestaHubApp(mainViewModel, state.user)
                        }
                        is AuthState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {
                            AuthScreen(mainViewModel, authViewModel)
                        }
                    }
                }
            }
        }
    }
}