package br.com.gestahub.ui.weight.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gestahub.ui.weight.WeightChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@Composable
fun ChartCard(
    weightEntries: List<FloatEntry>,
    dateLabels: List<String>
) {
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(key1 = weightEntries) {
        chartEntryModelProducer.setEntries(weightEntries)
    }

    val minY = weightEntries.minByOrNull { it.y }?.y?.minus(2)
    val maxY = weightEntries.maxByOrNull { it.y }?.y?.plus(2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Evolução do Peso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeightChart(
                chartEntryModelProducer = chartEntryModelProducer,
                dateLabels = dateLabels,
                minY = minY,
                maxY = maxY
            )
        }
    }
}