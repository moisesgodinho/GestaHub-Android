package br.com.gestahub.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
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
import kotlin.math.roundToInt


@Composable
fun WeightChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    dateLabels: List<String>,
    minY: Float?,
    maxY: Float?
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedWeight by remember { mutableStateOf<Float?>(null) } // 🟢 novo estado

    val bottomAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            dateLabels.getOrNull(value.toInt()) ?: ""
        }

    val actualMinY = minY ?: 0f
    val actualMaxY = maxY ?: 0f
    val steps = 9
    val stepValue = if (actualMaxY > actualMinY) {
        (actualMaxY - actualMinY) / steps
    } else 1f

    val yAxisValues = List(10) { i -> actualMinY + i * stepValue }

    val valueRange = abs(actualMaxY - actualMinY)
    val decimalPlaces = when {
        valueRange > 20 -> 0
        valueRange > 5 -> 1
        else -> 2
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
    val spacing = max(1, if (dateLabels.isNotEmpty()) dateLabels.size / desiredLabelCount else 1)

    val bottomAxisItemPlacer = AxisItemPlacer.Horizontal.default(
        spacing = spacing,
        offset = 0,
        shiftExtremeTicks = true
    )

    val axisLabelColor = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxWidth()) {
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
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width
                        val xPercent = (offset.x / chartWidth).coerceIn(0f, 1f)
                        val clickedIndex = (xPercent * (dateLabels.size - 1)).roundToInt()

                        if (clickedIndex in dateLabels.indices) {
                            selectedIndex = clickedIndex
                            selectedDate = dateLabels[clickedIndex]

                            // ✅ acesso seguro ao modelo
                            chartEntryModelProducer.getModel()?.let { model ->
                                val entries = model.entries.firstOrNull()
                                selectedWeight = entries?.getOrNull(clickedIndex)?.y
                            }
                        }
                    }
                }
        )

        // ✅ Tooltip com data e peso
        if (selectedIndex != null && selectedDate != null && selectedWeight != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        color = Rose500.copy(alpha = 0.9f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${selectedDate!!} • ${"%.1f".format(selectedWeight)} kg",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }}
