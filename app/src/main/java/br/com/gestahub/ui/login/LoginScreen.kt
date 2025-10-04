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
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderImage()
            Spacer(modifier = Modifier.height(32.dp))
            WelcomeText()
            Spacer(modifier = Modifier.height(48.dp))
            GoogleSignInButton(onClick = onSignInClick)
        }
    }
}

@Composable
fun HeaderImage() {
    Image(
        painter = painterResource(id = R.drawable.login),
        contentDescription = "Ilustração de uma gestante",
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(1f),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun WelcomeText() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Bem-vinda ao",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "GestaHub",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary // <-- CORRIGIDO AQUI
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Seu companheiro de bolso para uma jornada gestacional mais tranquila e informada.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
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
                tint = Color.Unspecified // Para manter a cor original do logo
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Entrar com Google",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    GestaHubTheme {
        LoginScreen(onSignInClick = {})
    }
}