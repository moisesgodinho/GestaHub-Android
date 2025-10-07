package br.com.gestahub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.login.AuthState
import br.com.gestahub.ui.login.AuthScreen
import br.com.gestahub.ui.login.AuthViewModel
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.navigation.GestaHubApp
import br.com.gestahub.ui.theme.GestaHubTheme


class MainActivity : ComponentActivity() {
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