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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.domain.model.AnnualSummaryRow
import com.example.ui.theme.LoanLabColors

@OptIn(ExperimentalTextApi::class)
@Composable
fun AnnualStackedBarChart(
    rows: List<AnnualSummaryRow>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(rows) {
        animProgress.animateTo(1f, tween(850))
    }

    val textMeasurer = rememberTextMeasurer()

    // Sample down to last 10 years to fit perfectly on standard mobile screen boundaries
    val visibleRows = remember(rows) {
        if (rows.size <= 10) rows else rows.takeLast(10)
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (visibleRows.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val paddingX = 40f
            val paddingY = 50f

            val chartWidth = width - 2 * paddingX
            val chartHeight = height - 2 * paddingY

            // Max value is the sum of interest and principal only (prepayment removed from chart to avoid cluttering)
            val maxOutflow = visibleRows.maxOf { it.interestTotal + it.principalTotal }.toFloat()
            if (maxOutflow <= 0) return@Canvas

            val count = visibleRows.size
            val groupWidth = chartWidth / count
            val barWidth = groupWidth * 0.45f

            // Draw baseline guides
            val baselineCount = 3
            for (i in 0..baselineCount) {
                val y = paddingY + (i.toFloat() / baselineCount) * chartHeight
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(paddingX, y),
                    end = Offset(width - paddingX, y),
                    strokeWidth = 2f
                )
            }

            visibleRows.forEachIndexed { index, r ->
                val x = paddingX + (index * groupWidth) + (groupWidth - barWidth) / 2

                // Heights
                val interestHeight = (r.interestTotal.toFloat() / maxOutflow) * chartHeight * animProgress.value
                val principalHeight = (r.principalTotal.toFloat() / maxOutflow) * chartHeight * animProgress.value

                var currentY = height - paddingY

                // Stack 1: Interest (ChartRed)
                if (interestHeight > 0) {
                    currentY -= interestHeight
                    drawRect(
                        color = LoanLabColors.ChartRed,
                        topLeft = Offset(x, currentY),
                        size = Size(barWidth, interestHeight)
                    )
                }

                // Stack 2: Principal (ChartBlue)
                if (principalHeight > 0) {
                    currentY -= principalHeight
                    drawRect(
                        color = LoanLabColors.ChartBlue,
                        topLeft = Offset(x, currentY),
                        size = Size(barWidth, principalHeight)
                    )
                }

                // Year Label
                val measuredLabel = textMeasurer.measure(
                    text = "Y${r.yearNo}",
                    style = TextStyle(color = LoanLabColors.Text2, fontSize = 11.sp)
                )
                drawText(
                    textLayoutResult = measuredLabel,
                    topLeft = Offset(
                        x + (barWidth - measuredLabel.size.width) / 2,
                        height - paddingY + 8f
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
