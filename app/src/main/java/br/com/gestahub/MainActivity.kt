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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MonitorWeight // <-- ADICIONE ESTE IMPORT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.gestahub.ui.calculator.CalculatorScreen
import br.com.gestahub.ui.components.AppHeader
import br.com.gestahub.ui.home.GestationalDataState
import br.com.gestahub.ui.home.HomeScreen
import br.com.gestahub.ui.login.AuthState
import br.com.gestahub.ui.login.AuthViewModel
import br.com.gestahub.ui.login.LoginScreen
import br.com.gestahub.ui.main.MainViewModel
import br.com.gestahub.ui.placeholder.ComingSoonScreen
import br.com.gestahub.ui.profile.EditProfileScreen
import br.com.gestahub.ui.profile.ProfileScreen
import br.com.gestahub.ui.theme.GestaHubTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(mainViewModel: MainViewModel, user: FirebaseUser) {
    val homeViewModel: br.com.gestahub.ui.home.HomeViewModel = viewModel(key = user.uid)
    val homeUiState by homeViewModel.uiState.collectAsState()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- LISTA DE ITENS REORDENADA ---
    val navItems = listOf(
        NavItem("Início", Icons.Default.Home, "home"),
        NavItem("Consultas", Icons.Default.CalendarMonth, "appointments"),
        NavItem("Diário", Icons.Default.Book, "journal"),
        NavItem("Peso", Icons.Filled.MonitorWeight, "weight"), // <-- NOVO ITEM
        NavItem("Mais", Icons.Default.MoreHoriz, "more")
    )

    val showBottomBar = navItems.any { it.route == currentRoute }
    val showMainHeader = showBottomBar

    Scaffold(
        topBar = {
            if (showMainHeader) {
                AppHeader(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { mainViewModel.toggleTheme() },
                    onProfileClick = { navController.navigate("profile") }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    onAddDataClick = { navController.navigate("calculator") },
                    onEditDataClick = {
                        val dataState = homeUiState.dataState
                        if (dataState is GestationalDataState.HasData) {
                            val data = dataState.gestationalData
                            navController.navigate(
                                "calculator?lmp=${data.lmp ?: ""}&examDate=${data.ultrasoundExamDate ?: ""}&weeks=${data.weeksAtExam ?: ""}&days=${data.daysAtExam ?: ""}"
                            )
                        }
                    }
                )

                LaunchedEffect(homeUiState.dataState) {
                    if (homeUiState.dataState is GestationalDataState.NoData) {
                        navController.navigate("calculator") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            }

            // Novas rotas
            composable("appointments") { ComingSoonScreen() }
            composable("journal") { ComingSoonScreen() }
            composable("weight") { ComingSoonScreen() } // <-- NOVA ROTA
            composable("more") { ComingSoonScreen() }

            // Rotas existentes
            composable("calculator?lmp={lmp}&examDate={examDate}&weeks={weeks}&days={days}") { backStackEntry ->
                CalculatorScreen(
                    onSaveSuccess = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    onCancelClick = { navController.popBackStack() },
                    initialLmp = backStackEntry.arguments?.getString("lmp"),
                    initialExamDate = backStackEntry.arguments?.getString("examDate"),
                    initialWeeks = backStackEntry.arguments?.getString("weeks"),
                    initialDays = backStackEntry.arguments?.getString("days"),
                )
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
                            MainAppScreen(mainViewModel, state.user)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(mainViewModel: MainViewModel, authViewModel: AuthViewModel) {
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