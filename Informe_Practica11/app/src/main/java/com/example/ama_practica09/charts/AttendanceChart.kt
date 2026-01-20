package com.example.ama_practica09.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Gráfico de barras animado con Canvas
 */
@Composable
fun BarChart(
    data: List<ChartDataPoint>,
    config: ChartConfig = ChartConfig(),
    modifier: Modifier = Modifier
) {
    // Animación de progreso
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = config.animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = config.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Canvas del gráfico
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = data.size

            if (barCount == 0) return@Canvas

            // Calcular dimensiones
            val spacing = canvasWidth * 0.02f
            val barWidth = (canvasWidth - (spacing * (barCount + 1))) / barCount
            val maxValue = config.maxValue ?: ChartDataProcessor.calculateMaxYValue(data.map { it.value })

            // Dibujar grid si está habilitado
            if (config.showGrid) {
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                for (i in 0..5) {
                    val y = canvasHeight * (i / 5f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }

            // Dibujar barras
            data.forEachIndexed { index, dataPoint ->
                val x = spacing + (index * (barWidth + spacing))
                val normalizedValue = (dataPoint.value / maxValue).coerceIn(0f, 1f)
                val barHeight = canvasHeight * normalizedValue * animationProgress.value

                // Color dinámico basado en el valor
                val barColor = dataPoint.color ?: ChartDataProcessor.getColorForValue(
                    value = dataPoint.value,
                    maxValue = maxValue
                )

                // Dibujar barra con esquinas redondeadas
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, canvasHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Dibujar etiqueta del valor encima de la barra
                if (config.showLabels && animationProgress.value > 0.8f) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 28f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawText(
                            dataPoint.value.toInt().toString(),
                            x + barWidth / 2,
                            canvasHeight - barHeight - 10f,
                            paint
                        )
                    }
                }

                // Dibujar etiqueta del eje X
                if (config.showLabels) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            dataPoint.label,
                            x + barWidth / 2,
                            canvasHeight + 40f,
                            paint
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gráfico de líneas animado con Canvas
 */
@Composable
fun LineChart(
    data: List<ChartDataPoint>,
    config: ChartConfig = ChartConfig(),
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = config.animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = config.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Canvas del gráfico
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val pointCount = data.size

            if (pointCount < 2) return@Canvas

            val maxValue = config.maxValue ?: ChartDataProcessor.calculateMaxYValue(data.map { it.value })
            val spacing = canvasWidth / (pointCount - 1)

            // Dibujar grid
            if (config.showGrid) {
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                for (i in 0..5) {
                    val y = canvasHeight * (i / 5f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }

            // Crear path para la línea
            val path = Path()
            val points = mutableListOf<Offset>()

            data.forEachIndexed { index, dataPoint ->
                val x = index * spacing
                val normalizedValue = (dataPoint.value / maxValue).coerceIn(0f, 1f)
                val y = canvasHeight - (canvasHeight * normalizedValue)

                points.add(Offset(x, y))

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Dibujar línea con animación
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 6f,
                    pathEffect = PathEffect.cornerPathEffect(20f) // Suavizado
                ),
                alpha = animationProgress.value
            )

            // Dibujar puntos
            points.forEachIndexed { index, point ->
                if (animationProgress.value > 0.5f) {
                    val dataPoint = data[index]
                    val pointColor = dataPoint.color ?: ChartDataProcessor.getColorForValue(
                        value = dataPoint.value,
                        maxValue = maxValue
                    )

                    drawCircle(
                        color = pointColor,
                        radius = 8f,
                        center = point
                    )

                    // Dibujar valor
                    if (config.showLabels) {
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                            drawText(
                                dataPoint.value.toInt().toString(),
                                point.x,
                                point.y - 15f,
                                paint
                            )
                        }
                    }

                    // Dibujar etiqueta del eje X
                    if (config.showLabels) {
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 32f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText(
                                dataPoint.label,
                                point.x,
                                canvasHeight + 40f,
                                paint
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gráfico circular (Pie Chart) animado
 */
@Composable
fun PieChart(
    data: List<ChartDataPoint>,
    config: ChartConfig = ChartConfig(),
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = config.animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = config.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Canvas del gráfico
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = (canvasWidth.coerceAtMost(canvasHeight) * 0.4f) * animationProgress.value
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2

            val total = data.sumOf { it.value.toDouble() }.toFloat()
            var startAngle = -90f

            data.forEachIndexed { index, dataPoint ->
                val sweepAngle = (dataPoint.value / total) * 360f

                val sliceColor = dataPoint.color ?: ChartDataProcessor.getColorForValue(
                    value = index.toFloat(),
                    maxValue = data.size.toFloat()
                )

                drawArc(
                    color = sliceColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Dibujar etiqueta
                if (config.showLabels && sweepAngle > 15f) {
                    val angle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val labelRadius = radius * 0.7f
                    val labelX = centerX + (labelRadius * Math.cos(angle)).toFloat()
                    val labelY = centerY + (labelRadius * Math.sin(angle)).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawText(
                            "${dataPoint.value.toInt()}",
                            labelX,
                            labelY + 10f,
                            paint
                        )
                    }
                }

                startAngle += sweepAngle
            }
        }

        // Leyenda
        Column(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            data.forEachIndexed { index, dataPoint ->
                val color = dataPoint.color ?: ChartDataProcessor.getColorForValue(
                    value = index.toFloat(),
                    maxValue = data.size.toFloat()
                )

                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.1f)
                            .background(color)
                    )
                    Text(
                        text = "${dataPoint.label}: ${dataPoint.value.toInt()}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
