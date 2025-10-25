package br.com.gestahub.ui.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestahub.ui.theme.Rose500
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

private fun Path.cubicTo(p1: Offset, p2: Offset, p3: Offset) {
    cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
}

@Composable
fun WeightChart(
    weightData: List<SimpleChartEntry>,
    dateLabels: List<String>,
    isDarkTheme: Boolean
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Cores
    val lineAndFillColor = Rose500
    val gridColor = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
    val textColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    val tooltipBackgroundColor = if (isDarkTheme) Color.DarkGray.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val tooltipTextColor = if (isDarkTheme) Color.White else Color.Black

    val textMeasurer = rememberTextMeasurer()

    // Lógica para os eixos
    val minY = weightData.minOfOrNull { it.y } ?: 0f
    val maxY = weightData.maxOfOrNull { it.y } ?: 100f
    val paddedMinY = floor(minY - 2)
    val paddedMaxY = ceil(maxY + 2)
    val yRange = paddedMaxY - paddedMinY
    val numGridLines = 5
    val yStep = if (yRange > 0) yRange / numGridLines else 1f


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .pointerInput(weightData.size) {
                detectTapGestures(
                    onPress = { offset ->
                        val yAxisAreaWidth = 40.dp.toPx()
                        val chartWidth = size.width - yAxisAreaWidth
                        val stepX = chartWidth / (weightData.size - 1).coerceAtLeast(1)
                        val tappedIndex = ((offset.x - yAxisAreaWidth) / stepX)
                            .roundToInt()
                            .coerceIn(0, weightData.size - 1)
                        selectedIndex = tappedIndex

                        val released = tryAwaitRelease()

                        if (released) {
                            selectedIndex = null
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val yAxisAreaWidth = 40.dp.toPx()
            val xAxisAreaHeight = 30.dp.toPx()
            val chartHeight = size.height - xAxisAreaHeight
            val chartWidth = size.width - yAxisAreaWidth
            val stepX = chartWidth / (weightData.size - 1).coerceAtLeast(1)

            // Desenhar Eixo Y (Grid e Labels)
            for (i in 0..numGridLines) {
                val yValue = paddedMinY + (i * yStep)
                val y = chartHeight - ((yValue - paddedMinY) / yRange) * chartHeight
                drawLine(
                    color = gridColor,
                    start = Offset(yAxisAreaWidth, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                val measuredText = textMeasurer.measure(
                    text = yValue.roundToInt().toString(),
                    style = TextStyle(color = textColor, fontSize = 10.sp, textAlign = TextAlign.End)
                )
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(yAxisAreaWidth - measuredText.size.width - 4.dp.toPx(), y - (measuredText.size.height / 2))
                )
            }

            // Mapear pontos e desenhar gráfico
            val points = weightData.mapIndexed { index, value ->
                val y = chartHeight - ((value.y - paddedMinY) / yRange * chartHeight).coerceIn(0f, chartHeight)
                Offset(yAxisAreaWidth + (index * stepX), y)
            }

            val linePath = Path()
            val fillPath = Path()

            if (points.isNotEmpty()) {
                linePath.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, chartHeight)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p0 = points.getOrElse(i - 1) { points[i] }
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val p3 = points.getOrElse(i + 2) { p2 }
                    val controlPoint1 = Offset(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f)
                    val controlPoint2 = Offset(p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f)
                    linePath.cubicTo(controlPoint1, controlPoint2, p2)
                    fillPath.cubicTo(controlPoint1, controlPoint2, p2)
                }

                fillPath.lineTo(points.last().x, chartHeight)
                fillPath.close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineAndFillColor.copy(alpha = 0.3f), Color.Transparent),
                    endY = chartHeight
                )
            )
            drawPath(path = linePath, color = lineAndFillColor, style = Stroke(width = 2.dp.toPx()))


            // Desenhar Eixo X (Labels)
            val numLabels = 7
            val xLabelStep = (dateLabels.size / numLabels).coerceAtLeast(1)
            (dateLabels.indices step xLabelStep).forEach { index ->
                val x = yAxisAreaWidth + (index * stepX)
                val measuredText = textMeasurer.measure(
                    text = dateLabels[index],
                    style = TextStyle(color = textColor, fontSize = 10.sp, textAlign = TextAlign.Center)
                )
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(x - (measuredText.size.width / 2), chartHeight + 4.dp.toPx())
                )
            }

            // Desenhar Tooltip
            selectedIndex?.let { index ->
                val point = points.getOrNull(index) ?: return@let
                val x = point.x

                drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, chartHeight), strokeWidth = 1.dp.toPx())
                drawCircle(color = lineAndFillColor, radius = 5.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)

                val date = dateLabels.getOrNull(index) ?: ""
                val weight = weightData.getOrNull(index)?.y ?: 0f
                val tooltipText = "$date • ${"%.1f".format(weight)} kg"
                val measuredText = textMeasurer.measure(
                    text = tooltipText,
                    style = TextStyle(color = tooltipTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )

                val tooltipPadding = 8.dp.toPx()
                val tooltipWidth = measuredText.size.width + tooltipPadding * 2
                val tooltipHeight = measuredText.size.height + tooltipPadding
                var tooltipX = x - (tooltipWidth / 2)
                if (tooltipX < yAxisAreaWidth) tooltipX = yAxisAreaWidth
                if (tooltipX + tooltipWidth > size.width) tooltipX = size.width - tooltipWidth
                val tooltipY = point.y - tooltipHeight - 8.dp.toPx()

                drawRoundRect(
                    color = tooltipBackgroundColor,
                    topLeft = Offset(tooltipX, if (tooltipY > 0) tooltipY else 0f),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(tooltipX + tooltipPadding, (if (tooltipY > 0) tooltipY else 0f) + tooltipPadding / 2)
                )
            }
        }
    }
}