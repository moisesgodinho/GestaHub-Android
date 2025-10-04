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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.gestahub.ui.calculator.CalculatorScreen
import br.com.gestahub.ui.components.AppHeader
import br.com.gestahub.ui.home.HomeScreen
import br.com.gestahub.ui.login.AuthState
import br.com.gestahub.ui.login.AuthViewModel
import br.com.gestahub.ui.login.LoginScreen
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.profile.EditProfileScreen
import br.com.gestahub.ui.profile.ProfileScreen
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

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // O cabeçalho principal só será exibido se a rota atual NÃO for a de perfil ou edição
    val showMainHeader = currentRoute != "profile" && currentRoute != "editProfile"

    Scaffold(
        topBar = {
            if (showMainHeader) {
                AppHeader(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { mainViewModel.toggleTheme() },
                    onProfileClick = { navController.navigate("profile") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                Box(modifier = Modifier.padding(innerPadding)) {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onEditDataClick = {
                            val data = homeUiState.gestationalData
                            navController.navigate(
                                "calculator?lmp=${data.lmp ?: ""}&examDate=${data.ultrasoundExamDate ?: ""}&weeks=${data.weeksAtExam ?: ""}&days=${data.daysAtExam ?: ""}"
                            )
                        }
                    )
                }

                LaunchedEffect(homeUiState.hasData, homeUiState.isLoading) {
                    if (!homeUiState.isLoading && !homeUiState.hasData) {
                        navController.navigate("calculator")
                    }
                }
            }

            composable("calculator?lmp={lmp}&examDate={examDate}&weeks={weeks}&days={days}") { backStackEntry ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    CalculatorScreen(
                        onSaveSuccess = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        onCancelClick = { navController.popBackStack() },
                        initialLmp = backStackEntry.arguments?.getString("lmp"),
                        initialExamDate = backStackEntry.arguments?.getString("examDate"),
                        initialWeeks = backStackEntry.arguments?.getString("weeks"),
                        initialDays = backStackEntry.arguments?.getString("days"),
                    )
                }
            }

            composable("profile") {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { navController.navigate("editProfile") }
                )
            }

            composable("editProfile") {
                EditProfileScreen(
                    onSaveSuccess = { navController.popBackStack() },
                    onCancelClick = { navController.popBackStack() }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(mainViewModel: MainViewModel, authViewModel: AuthViewModel = viewModel()) {
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

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = authState) {
                is AuthState.Idle, is AuthState.Error -> {
                    if (state is AuthState.Error) {
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
                is AuthState.Success -> {}
            }
        }
    }
}