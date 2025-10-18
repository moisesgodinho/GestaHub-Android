package br.com.gestahub.ui.hydration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun HydrationChartCard(
    history: List<WaterIntakeEntry>,
    displayedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    isDarkTheme: Boolean,
    selectedDayIndex: Int?,
    onDaySelected: (Int?) -> Unit
) {
    val entriesByMonth = remember(history) {
        history.groupBy { YearMonth.parse(it.date?.substring(0, 7)) }
    }

    val sortedMonths = remember(entriesByMonth) {
        entriesByMonth.keys.sortedDescending()
    }

    val firstMonthWithData = sortedMonths.lastOrNull()
    val currentMonth = YearMonth.now()

    val isPreviousButtonEnabled = firstMonthWithData != null && displayedMonth > firstMonthWithData
    val isNextButtonEnabled = displayedMonth < currentMonth

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onMonthChange(displayedMonth.minusMonths(1)) },
                    enabled = isPreviousButtonEnabled
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
                }

                Text(
                    text = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR")))
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { onMonthChange(displayedMonth.plusMonths(1)) },
                    enabled = isNextButtonEnabled
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MonthlyHydrationChart(
                entries = entriesByMonth[displayedMonth] ?: emptyList(),
                yearMonth = displayedMonth,
                isDarkTheme = isDarkTheme,
                selectedDayIndex = selectedDayIndex,
                onDaySelected = onDaySelected
            )
        }
    }
}

private fun Path.cubicTo(p1: Offset, p2: Offset, p3: Offset) {
    cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
}

@Composable
private fun MonthlyHydrationChart(
    entries: List<WaterIntakeEntry>,
    yearMonth: YearMonth,
    isDarkTheme: Boolean,
    selectedDayIndex: Int?,
    onDaySelected: (Int?) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()

    val data = remember(entries, daysInMonth) {
        val dailyTotals = FloatArray(daysInMonth) { 0f }
        entries.forEach { entry ->
            val dayOfMonth = entry.date?.substring(8, 10)?.toIntOrNull()
            if (dayOfMonth != null && dayOfMonth in 1..daysInMonth) {
                dailyTotals[dayOfMonth - 1] = entry.current.toFloat()
            }
        }
        dailyTotals.toList()
    }

    val goal = entries.maxByOrNull { it.date ?: "" }?.goal?.toFloat() ?: 2500f

    val maxValueRaw = maxOf(data.maxOrNull() ?: 0f, goal)
    val yStep = if (maxValueRaw <= 5000f) 500f else 1000f
    var maxValue = ceil(maxValueRaw / yStep) * yStep
    if (maxValue == 0f) {
        maxValue = if (yStep == 500f) 2500f else 5000f
    }

    // Cores
    val lineAndFillColor = Color(0xFF64B5F6)
    val goalLineColor = if (isDarkTheme) Color(0xFFF57C00) else Color(0xFFFF9800)
    val gridColor = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
    val textColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    val tooltipBackgroundColor = if (isDarkTheme) Color.DarkGray.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val tooltipTextColor = if (isDarkTheme) Color.White else Color.Black

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(daysInMonth) {
                detectTapGestures(
                    onTap = { offset ->
                        val yAxisAreaWidth = 40.dp.toPx()
                        val xAxisAreaHeight = 20.dp.toPx()
                        val chartHeight = size.height - xAxisAreaHeight
                        val chartWidth = size.width - yAxisAreaWidth

                        // Verifica se o toque foi dentro da área interativa do gráfico
                        val isInsideChartArea = offset.x >= yAxisAreaWidth && offset.y <= chartHeight

                        if (isInsideChartArea) {
                            // Se o toque foi dentro, calcula o dia e alterna a tooltip
                            val stepX = chartWidth / (daysInMonth - 1).coerceAtLeast(1)
                            val tappedIndex = ((offset.x - yAxisAreaWidth) / stepX).roundToInt().coerceIn(0, daysInMonth - 1)
                            onDaySelected(if (selectedDayIndex == tappedIndex) null else tappedIndex)
                        } else {
                            // Se o toque foi fora, esconde a tooltip
                            onDaySelected(null)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // --- ESPAÇAMENTO DO EIXO Y CORRIGIDO AQUI ---
            val yAxisAreaWidth = 40.dp.toPx()
            val xAxisAreaHeight = 20.dp.toPx()
            val chartHeight = size.height - xAxisAreaHeight
            val chartWidth = size.width - yAxisAreaWidth
            val stepX = chartWidth / (daysInMonth - 1).coerceAtLeast(1)

            // --- DESENHAR EIXO Y ---
            var yValue = 0f
            while (yValue <= maxValue) {
                val y = chartHeight - (yValue / maxValue) * chartHeight
                drawLine(
                    color = gridColor,
                    start = Offset(yAxisAreaWidth, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )

                val labelText = "${yValue.toInt()}"
                val measuredText = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(color = textColor, fontSize = 10.sp, textAlign = TextAlign.End)
                )
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(yAxisAreaWidth - measuredText.size.width - 4.dp.toPx(), y - (measuredText.size.height / 2))
                )
                yValue += yStep
            }

            // --- DESENHAR GRÁFICO ---
            val goalY = chartHeight - (goal / maxValue) * chartHeight
            if (goalY > 0 && goalY < chartHeight) {
                for (i in 0 until chartWidth.toInt() step 20) {
                    drawLine(
                        color = goalLineColor,
                        start = Offset(yAxisAreaWidth + i, goalY),
                        end = Offset(yAxisAreaWidth + i + 10, goalY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            val points = data.mapIndexed { index, value ->
                Offset(yAxisAreaWidth + (index * stepX), (chartHeight - (value / maxValue) * chartHeight).coerceAtMost(chartHeight))
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

                    // Calcula a posição X dos pontos de controle
                    val controlPoint1X = p1.x + (p2.x - p0.x) / 6f
                    val controlPoint2X = p2.x - (p3.x - p1.x) / 6f

                    // Calcula a posição Y original dos pontos de controle
                    val controlPoint1Y = (p1.y + (p2.y - p0.y) / 6f)
                    val controlPoint2Y = (p2.y - (p3.y - p1.y) / 6f)

                    // Define os limites mínimo e máximo para a curva
                    val minY = minOf(p1.y, p2.y)
                    val maxY = maxOf(p1.y, p2.y)

                    // Limita ("clampa") a posição Y dos pontos de controle para evitar o "overshoot"
                    val clampedControlPoint1Y = controlPoint1Y.coerceIn(minY, maxY)
                    val clampedControlPoint2Y = controlPoint2Y.coerceIn(minY, maxY)

                    // Cria os novos pontos de controle com os valores Y ajustados
                    val controlPoint1 = Offset(controlPoint1X, clampedControlPoint1Y.coerceAtMost(chartHeight))
                    val controlPoint2 = Offset(controlPoint2X, clampedControlPoint2Y.coerceAtMost(chartHeight))

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

            // --- DESENHAR EIXO X ---
            val xAxisStep = if (daysInMonth > 20) 4 else 2
            (0 until daysInMonth step xAxisStep).forEach { dayIndex ->
                val day = dayIndex + 1
                val x = yAxisAreaWidth + (dayIndex * stepX)
                val measuredText = textMeasurer.measure(
                    text = day.toString(),
                    style = TextStyle(color = textColor, fontSize = 10.sp, textAlign = TextAlign.Center)
                )
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(x - (measuredText.size.width / 2), chartHeight + 4.dp.toPx())
                )
            }

            // --- LÓGICA PARA DESENHAR A TOOLTIP ---
            selectedDayIndex?.let { index ->
                val point = points.getOrNull(index) ?: return@let
                val x = point.x

                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 1.dp.toPx()
                )

                drawCircle(color = lineAndFillColor, radius = 5.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)

                val day = index + 1
                val consumption = data.getOrNull(index)?.toInt() ?: 0
                val tooltipText = "$consumption ml - Dia $day"
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

                // --- POSIÇÃO DA TOOLTIP CORRIGIDA ---
                val tooltipY = point.y - tooltipHeight - 8.dp.toPx() // 8.dp de margem acima do ponto

                drawRoundRect(
                    color = tooltipBackgroundColor,
                    topLeft = Offset(tooltipX, if (tooltipY > 0) tooltipY else 0f), // Garante que não saia do topo
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