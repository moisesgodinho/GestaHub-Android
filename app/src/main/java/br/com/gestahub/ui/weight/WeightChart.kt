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
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import br.com.gestahub.ui.theme.Rose500
import kotlin.math.abs
import kotlin.math.max


@Composable
fun WeightChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    dateLabels: List<String>,
    minY: Float?,
    maxY: Float?
) {
    val bottomAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            dateLabels.getOrNull(value.toInt()) ?: ""
        }

    // --- Cálculo de 10 valores igualmente espaçados ---
    val actualMinY = minY ?: 0f
    val actualMaxY = maxY ?: 0f
    val steps = 9 // 10 labels → 9 intervalos
    val stepValue = if (actualMaxY > actualMinY) {
        (actualMaxY - actualMinY) / steps
    } else 1f

    val yAxisValues = List(10) { i -> actualMinY + i * stepValue }

    // --- Formatação automática ---
    val valueRange = abs(actualMaxY - actualMinY)
    val decimalPlaces = when {
        valueRange > 20 -> 0 // intervalos grandes → sem decimais
        valueRange > 5 -> 1  // médio → 1 decimal
        else -> 2            // pequeno → mais precisão
    }

    val yAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            val closest = yAxisValues.minByOrNull { kotlin.math.abs(it - value) } ?: value
            if (kotlin.math.abs(value - closest) < stepValue / 2) {
                "%.${decimalPlaces}f kg".format(closest)
            } else ""
        }

    val verticalItemPlacer = AxisItemPlacer.Vertical.default(
        maxItemCount = 10
    )

    val desiredLabelCount = 7
    val spacing = max(1, dateLabels.size / desiredLabelCount)

    val bottomAxisItemPlacer = AxisItemPlacer.Horizontal.default(
        spacing = spacing,
        offset = 0,
        shiftExtremeTicks = true
    )

    val axisLabelColor = MaterialTheme.colorScheme.onBackground

    Chart(
        chart = lineChart(
            lines = listOf(
                LineChart.LineSpec(
                    lineColor = Rose500.toArgb(),
                    lineThicknessDp = 3f,
                    lineBackgroundShader = verticalGradient(
                        arrayOf(
                            Rose500.copy(alpha = 0.4f),
                            Rose500.copy(alpha = 0.0f)
                        ),
                    ),
                    point = null
                )
            ),
            axisValuesOverrider = AxisValuesOverrider.fixed(
                minY = actualMinY,
                maxY = actualMaxY
            )
        ),
        chartModelProducer = chartEntryModelProducer,
        startAxis = rememberStartAxis(
            valueFormatter = yAxisValueFormatter,
            itemPlacer = verticalItemPlacer,
            label = textComponent {
                color = axisLabelColor.toArgb()
            },
            guideline = null,
            tickLength = 0.dp
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = bottomAxisValueFormatter,
            guideline = null,
            itemPlacer = bottomAxisItemPlacer,
            labelRotationDegrees = 25f,
            label = textComponent {
                color = axisLabelColor.toArgb()
            }
        ),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
