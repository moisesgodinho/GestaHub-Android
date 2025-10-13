package br.com.gestahub.ui.weight.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.theme.Rose500

// Definimos a enum BmiCategory aqui, pois este é o principal componente que a utiliza para lógica de UI.
// Isso ajuda a manter o WeightScreen.kt ainda mais limpo.
enum class BmiCategory {
    LOW, NORMAL, OVERWEIGHT, OBESE
}

// A função que determina a categoria também vem para cá, junto de quem a usa.
fun getBmiCategory(bmi: Double): BmiCategory? {
    return when {
        bmi < 18.5 -> BmiCategory.LOW
        bmi >= 18.5 && bmi < 25.0 -> BmiCategory.NORMAL
        bmi >= 25.0 && bmi < 30.0 -> BmiCategory.OVERWEIGHT
        bmi >= 30.0 -> BmiCategory.OBESE
        else -> null
    }
}

@Composable
fun WeightGainRecommendationsCard(initialBmi: Double, isDarkTheme: Boolean) {
    val currentCategory = getBmiCategory(initialBmi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recomendações de Ganho de Peso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A meta de ganho de peso é baseada no seu IMC pré-gestacional.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RecommendationItem(
                    category = "Baixo Peso",
                    imcRange = "IMC < 18.5",
                    recommendedGain = "12.5 a 18 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.LOW
                )
                RecommendationItem(
                    category = "Peso Adequado",
                    imcRange = "IMC 18.5 - 24.9",
                    recommendedGain = "11.5 a 16 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.NORMAL
                )
                RecommendationItem(
                    category = "Sobrepeso",
                    imcRange = "IMC 25.0 - 29.9",
                    recommendedGain = "7 a 11.5 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.OVERWEIGHT
                )
                RecommendationItem(
                    category = "Obesidade",
                    imcRange = "IMC ≥ 30.0",
                    recommendedGain = "5 a 9 kg",
                    isDarkTheme = isDarkTheme,
                    isHighlighted = currentCategory == BmiCategory.OBESE
                )
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    category: String,
    imcRange: String,
    recommendedGain: String,
    isDarkTheme: Boolean,
    isHighlighted: Boolean
) {
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val categoryStyle = if (isHighlighted) {
        SpanStyle(fontWeight = FontWeight.SemiBold, color = Rose500)
    } else {
        SpanStyle(fontWeight = FontWeight.SemiBold)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (isHighlighted) Rose500 else Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = categoryStyle) {
                            append("$category ")
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append("($imcRange)")
                        }
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ganho de peso total recomendado: $recommendedGain",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}