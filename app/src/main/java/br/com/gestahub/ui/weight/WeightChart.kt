package br.com.gestahub.ui.weight

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlin.math.max

// --- ARQUIVO MODIFICADO ---
@Composable
fun WeightChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    dateLabels: List<String>,
    minY: Float?, // Recebe o valor mínimo
    maxY: Float?  // Recebe o valor máximo
) {
    val bottomAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            dateLabels.getOrNull(value.toInt()) ?: ""
        }

    val yAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ -> "${value.toInt()} kg" }

    // A lógica de cálculo foi movida para o `ChartCard`
    val desiredLabelCount = 7
    val spacing = max(1, dateLabels.size / desiredLabelCount)

    val itemPlacer = AxisItemPlacer.Horizontal.default(
        spacing = spacing,
        offset = 0,
        shiftExtremeTicks = true
    )

    Chart(
        chart = lineChart(
            lines = listOf(
                LineChart.LineSpec(
                    lineColor = MaterialTheme.colorScheme.primary.toArgb(),
                    lineThicknessDp = 3f,
                    lineBackgroundShader = verticalGradient(
                        arrayOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                        ),
                    ),
                    point = null
                )
            ),
            // Usa os valores recebidos para definir o eixo Y
            axisValuesOverrider = if (minY != null && maxY != null) {
                AxisValuesOverrider.fixed(minY = minY, maxY = maxY)
            } else {
                AxisValuesOverrider.fixed()
            }
        ),
        chartModelProducer = chartEntryModelProducer,
        startAxis = rememberStartAxis(
            valueFormatter = yAxisValueFormatter
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = bottomAxisValueFormatter,
            guideline = null,
            itemPlacer = itemPlacer,
            labelRotationDegrees = 25f
        ),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}