package br.com.gestahub.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestahub.R
import br.com.gestahub.ui.theme.GestaHubTheme

@Composable
fun LoginScreen(onSignInClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Usa a cor de fundo do tema
    ) {
        // Usamos um Box para permitir que o botão seja empurrado para o final
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp) // Padding geral maior, como no PWA
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center), // Alinha a coluna de texto e imagem no centro
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderImage()
                Spacer(modifier = Modifier.height(48.dp)) // Espaço maior após a imagem
                WelcomeText()
            }

            // Botão posicionado na parte de baixo da tela
            GoogleSignInButton(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Alinha o botão na parte inferior do Box
            )
        }
    }
}

@Composable
fun HeaderImage() {
    Image(
        painter = painterResource(id = R.drawable.login),
        contentDescription = "Ilustração de uma gestante",
        modifier = Modifier
            .fillMaxWidth(0.8f) // Imagem um pouco maior
            .aspectRatio(1f),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun WelcomeText() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Bem-vinda ao",
            style = MaterialTheme.typography.headlineMedium, // Usando estilos do tema
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "GestaHub",
            style = MaterialTheme.typography.displaySmall, // Tamanho maior para o título
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp)) // Espaço maior para o subtítulo
        Text(
            text = "Seu companheiro de bolso para uma jornada gestacional mais tranquila e informada.",
            style = MaterialTheme.typography.bodyLarge, // Usando estilos do tema
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp), // Botão um pouco mais alto
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Logo do Google",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified // Mantém as cores originais do logo do Google
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Entrar com Google",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}


@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LoginScreenPreviewLight() {
    GestaHubTheme(darkTheme = false) {
        LoginScreen(onSignInClick = {})
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun LoginScreenPreviewDark() {
    GestaHubTheme(darkTheme = true) {
        LoginScreen(onSignInClick = {})
    }
}