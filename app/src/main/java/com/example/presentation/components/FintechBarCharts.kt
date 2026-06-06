package com.example.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.domain.model.ScenarioResult
import com.example.ui.theme.LoanLabColors
import com.example.ui.theme.BodySecond

@OptIn(ExperimentalTextApi::class)
@Composable
fun ScenarioBarChart(
    scenarios: List<ScenarioResult>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(scenarios) {
        animProgress.animateTo(1f, tween(800))
    }

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (scenarios.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val paddingX = 40f
            val paddingY = 60f

            val chartWidth = width - 2 * paddingX
            val chartHeight = height - 2 * paddingY

            val maxInterest = scenarios.maxOf { it.totalInterest }.toFloat()
            val minInterest = scenarios.minOf { it.totalInterest }
            if (maxInterest <= 0) return@Canvas

            val barCount = scenarios.size
            val groupWidth = chartWidth / barCount
            val barWidth = groupWidth * 0.5f

            // Draw baseline guides
            val baselineCount = 3
            for (i in 0..baselineCount) {
                val y = paddingY + (i.toFloat() / baselineCount) * chartHeight
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(paddingX, y),
                    end = Offset(width - paddingX, y),
                    strokeWidth = 2f
                )
            }

            scenarios.forEachIndexed { index, s ->
                // Apply dynamic intelligent coloring rules
                val color = when {
                    s.totalInterest == minInterest -> LoanLabColors.ChartGreen // Highlight the absolute interest saver plan in Green
                    s.scenarioName.contains("Base") || s.scenarioName.contains("Stress") -> LoanLabColors.ChartRed
                    else -> LoanLabColors.ChartBlue
                }

                val valueRatio = s.totalInterest.toFloat() / maxInterest
                val curBarHeight = valueRatio * chartHeight * animProgress.value

                val startX = paddingX + (index * groupWidth) + (groupWidth - barWidth) / 2
                val startY = height - paddingY - curBarHeight

                // Draw Bar
                drawRoundRect(
                    color = color,
                    topLeft = Offset(startX, startY),
                    size = Size(barWidth, curBarHeight),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                // Label below bar
                val labelLines = when (index) {
                    0 -> "Base Loan"
                    1 -> "Pay Less"
                    2 -> "Pay Faster"
                    else -> "Rate +2%"
                }

                val measuredText = textMeasurer.measure(
                    text = labelLines,
                    style = TextStyle(color = LoanLabColors.Text2, fontSize = 11.sp)
                )

                val textX = startX + barWidth / 2f
                val textY = height - paddingY + 10f

                translate(left = textX, top = textY) {
                    rotate(degrees = -30f, pivot = Offset(0f, 0f)) {
                        drawText(
                            textLayoutResult = measuredText,
                            topLeft = Offset(-measuredText.size.width / 2f, 0f)
                        )
                    }
                }

                // Numeric Interest total on top of bar
                val priceText = formatIndian(s.totalInterest)
                val measuredPrice = textMeasurer.measure(
                    text = priceText,
                    style = TextStyle(color = color, fontSize = 9.sp)
                )
                drawText(
                    textLayoutResult = measuredPrice,
                    topLeft = Offset(
                        startX + (barWidth - measuredPrice.size.width) / 2,
                        startY - measuredPrice.size.height - 6f
                    )
                )
            }

            // Bottom axis line
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(paddingX, height - paddingY),
                end = Offset(width - paddingX, height - paddingY),
                strokeWidth = 3f
            )
        }
    }
}
