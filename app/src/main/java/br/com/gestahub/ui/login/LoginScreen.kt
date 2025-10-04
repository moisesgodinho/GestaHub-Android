// Inserir em: src/main/java/br/com/gestahub/ui/login/LoginScreen.kt
package br.com.gestahub.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestahub.R
import br.com.gestahub.ui.theme.GestaHubTheme
import br.com.gestahub.ui.theme.Rose500

@Composable
fun LoginScreen(onSignInClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GestaHub",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Rose500
                )
                Text(
                    text = "Sua jornada da maternidade semana a semana.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.login),
                    contentDescription = "Ilustração da jornada da maternidade",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Parte Inferior: Features e Botão de Login
            Column(
                horizontalAlignment = Alignment.Start, // Alinhado à esquerda
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                FeatureList()
                Spacer(modifier = Modifier.height(24.dp))
                GoogleSignInButton(onClick = onSignInClick)
            }
        }
    }
}

@Composable
fun FeatureList() {
    val features = listOf(
        "Calculadoras precisas (DUM e Ultrassom)",
        "Diário de Sintomas e Humor",
        "Acompanhamento de Peso",
        "Contador de Movimentos com histórico",
        "Cronômetro de Contrações",
        "Cronograma de exames importantes"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.forEach { featureText ->
            FeatureItem(text = featureText)
        }
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Rose500,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Lembrete: Adicione um ícone do Google à sua pasta res/drawable
            // Ex: ic_google_logo.xml
            // Por enquanto, usaremos um ícone genérico se não tiver um.
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Entrar com Google", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }
    }
}

// Função para pré-visualizar a tela no Android Studio
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LoginScreenPreview() {
    GestaHubTheme {
        LoginScreen(onSignInClick = {})
    }
}