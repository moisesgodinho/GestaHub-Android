// Local do arquivo: app/src/main/java/br/com/gestahub/MainActivity.kt
package br.com.gestahub

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.calculator.CalculatorScreen
import br.com.gestahub.ui.components.AppHeader
import br.com.gestahub.ui.home.HomeScreen
import br.com.gestahub.ui.login.AuthState
import br.com.gestahub.ui.login.AuthViewModel
import br.com.gestahub.ui.login.LoginScreen
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.theme.GestaHubTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

            GestaHubTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Decide qual tela mostrar com base no estado de login do Firebase
                    if (Firebase.auth.currentUser == null) {
                        AuthScreen(mainViewModel)
                    } else {
                        MainAppScreen(mainViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(mainViewModel: MainViewModel) {
    val homeViewModel: br.com.gestahub.ui.home.HomeViewModel = viewModel()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

    // Controla se a tela da calculadora deve ser exibida
    var showCalculators by remember { mutableStateOf(!homeUiState.hasData) }

    // Efeito para atualizar 'showCalculators' quando os dados do Firebase chegam
    LaunchedEffect(homeUiState.hasData) {
        showCalculators = !homeUiState.hasData
    }

    Scaffold(
        topBar = {
            AppHeader(
                isDarkTheme = isDarkTheme,
                onThemeToggle = { mainViewModel.toggleTheme() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (showCalculators) {
                CalculatorScreen(onSaveSuccess = { showCalculators = false })
            } else {
                HomeScreen(onEditDataClick = { showCalculators = true })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(mainViewModel: MainViewModel, authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("AuthScreen", "Firebase auth with Google account ID: ${account.id}")
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            authViewModel.signInWithGoogleCredential(credential)
        } catch (e: ApiException) {
            Log.w("AuthScreen", "Google sign in failed", e)
            Toast.makeText(context, "Falha no login com Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            // O cabeçalho só é mostrado na tela principal, não no login
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = authState) {
                is AuthState.Idle, is AuthState.Error -> {
                    if(state is AuthState.Error) {
                        Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    LoginScreen(
                        onSignInClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        }
                    )
                }
                is AuthState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AuthState.Success -> {
                    // O estado de sucesso agora é gerenciado pela verificação inicial no setContent.
                    // Este bloco não será mais alcançado diretamente, pois o app recompõe para MainAppScreen.
                }
            }
        }
    }
}