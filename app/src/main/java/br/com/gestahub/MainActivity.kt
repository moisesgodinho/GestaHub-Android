// Cole este código no seu arquivo MainActivity.kt
package br.com.gestahub

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.login.AuthState
import br.com.gestahub.ui.login.AuthViewModel
import br.com.gestahub.ui.login.LoginScreen
import br.com.gestahub.ui.theme.GestaHubTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestaHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen()
                }
            }
        }
    }
}

@Composable
fun AuthScreen(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
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

    // Gerencia a UI com base no estado da autenticação
    when (val state = authState) {
        is AuthState.Idle -> {
            LoginScreen(
                onSignInClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)

                    // Garante que o usuário esteja deslogado do Google antes de tentar um novo login
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
            // Futuramente, aqui você navegará para a tela principal (Home)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Login bem-sucedido!", style = MaterialTheme.typography.headlineMedium)
            }
        }
        is AuthState.Error -> {
            // Em caso de erro, mostramos um Toast e voltamos para a tela de login
            Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
            LoginScreen( onSignInClick = { /* Ação de clique aqui */ } )
        }
    }
}