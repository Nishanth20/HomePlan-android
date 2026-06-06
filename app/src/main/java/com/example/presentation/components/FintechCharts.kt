package com.example.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthRow
import com.example.ui.theme.LoanLabColors
import com.example.ui.theme.HeroNumber
import com.example.ui.theme.BodySecond
import com.example.ui.theme.LabelText

@Composable
fun FintechAreaChart(
    monthRows: List<MonthRow>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(monthRows) {
        animProgress.animateTo(1f, tween(1000))
    }

    // Sample data to make drawing path calculations super fast
    val sampled = remember(monthRows) {
        if (monthRows.isEmpty()) emptyList()
        else {
            val step = maxOf(1, monthRows.size / 15)
            monthRows.filterIndexed { idx, _ -> idx % step == 0 || idx == monthRows.lastIndex }
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (sampled.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val paddingX = 40f
            val paddingY = 40f

            val chartWidth = width - 2 * paddingX
            val chartHeight = height - 2 * paddingY

            val maxVal = sampled.maxOf { maxOf(it.interest, it.principal) }.toFloat()
            if (maxVal <= 0) return@Canvas

            val pointsInterest = sampled.mapIndexed { idx, row ->
                val x = paddingX + (idx.toFloat() / (sampled.size - 1)) * chartWidth
                val y = height - paddingY - (row.interest.toFloat() / maxVal) * chartHeight
                Offset(x, y)
            }

            val pointsPrincipal = sampled.mapIndexed { idx, row ->
                val x = paddingX + (idx.toFloat() / (sampled.size - 1)) * chartWidth
                val y = height - paddingY - (row.principal.toFloat() / maxVal) * chartHeight
                Offset(x, y)
            }

            // Draw Background Grids
            val gridCount = 4
            for (i in 0..gridCount) {
                val y = paddingY + (i.toFloat() / gridCount) * chartHeight
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(paddingX, y),
                    end = Offset(width - paddingX, y),
                    strokeWidth = 2f
                )
            }

            // Draw Area & Path 1: Interest (ChartRed)
            val interestPath = Path().apply {
                if (pointsInterest.isNotEmpty()) {
                    moveTo(pointsInterest.first().x, pointsInterest.first().y)
                    for (i in 1 until pointsInterest.size) {
                        val endX = pointsInterest[i].x * animProgress.value
                        val endY = pointsInterest[i].y
                        lineTo(maxOf(pointsInterest.first().x, endX), endY)
                    }
                }
            }

            val interestFillPath = Path().apply {
                addPath(interestPath)
                if (pointsInterest.isNotEmpty()) {
                    lineTo(pointsInterest.last().x * animProgress.value, height - paddingY)
                    lineTo(pointsInterest.first().x, height - paddingY)
                    close()
                }
            }

            drawPath(
                path = interestFillPath,
                brush = Brush.verticalGradient(
                    listOf(LoanLabColors.ChartRed.copy(alpha = 0.25f), Color.Transparent)
                )
            )

            drawPath(
                path = interestPath,
                color = LoanLabColors.ChartRed,
                style = Stroke(width = 4f)
            )

            // Draw Area & Path 2: Principal (ChartBlue)
            val principalPath = Path().apply {
                if (pointsPrincipal.isNotEmpty()) {
                    moveTo(pointsPrincipal.first().x, pointsPrincipal.first().y)
                    for (i in 1 until pointsPrincipal.size) {
                        val endX = pointsPrincipal[i].x * animProgress.value
                        val endY = pointsPrincipal[i].y
                        lineTo(maxOf(pointsPrincipal.first().x, endX), endY)
                    }
                }
            }

            val principalFillPath = Path().apply {
                addPath(principalPath)
                if (pointsPrincipal.isNotEmpty()) {
                    lineTo(pointsPrincipal.last().x * animProgress.value, height - paddingY)
                    lineTo(pointsPrincipal.first().x, height - paddingY)
                    close()
                }
            }

            drawPath(
                path = principalFillPath,
                brush = Brush.verticalGradient(
                    listOf(LoanLabColors.ChartBlue.copy(alpha = 0.25f), Color.Transparent)
                )
            )

            drawPath(
                path = principalPath,
                color = LoanLabColors.ChartBlue,
                style = Stroke(width = 4f)
            )

            // Bottom Axis Line
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(paddingX, height - paddingY),
                end = Offset(width - paddingX, height - paddingY),
                strokeWidth = 3f
              )
          }
      }
}

@Composable
fun FintechDonutChart(
    interest: Double,
    principal: Double,
    prepayment: Double,
    totalPayment: Double,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(interest, principal, prepayment) {
        animProgress.animateTo(1f, tween(800))
    }

    val sum = interest + principal + prepayment
    val pctInterest = if (sum > 0) (interest / sum).toFloat() else 0f
    val pctPrincipal = if (sum > 0) (principal / sum).toFloat() else 0f
    val pctPrepayment = if (sum > 0) (prepayment / sum).toFloat() else 0f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = minOf(size.width, size.height) / 2 - 24.dp.toPx()

            val strokeWidth = 30.dp.toPx()

            var startAngle = -90f

            // 1. Interest Arc (ChartRed)
            val sweepInterest = pctInterest * 360f * animProgress.value
            drawArc(
                color = LoanLabColors.ChartRed,
                startAngle = startAngle,
                sweepAngle = sweepInterest,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepInterest

            // 2. Principal Arc (ChartBlue)
            val sweepPrincipal = pctPrincipal * 360f * animProgress.value
            drawArc(
                color = LoanLabColors.ChartBlue,
                startAngle = startAngle,
                sweepAngle = sweepPrincipal,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepPrincipal

            // 3. Prepayment Arc (ChartGreen)
            val sweepPrepay = pctPrepayment * 360f * animProgress.value
            drawArc(
                color = LoanLabColors.ChartGreen,
                startAngle = startAngle,
                sweepAngle = sweepPrepay,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
        }

        // Central visual statistics panel
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL ESCAPE COST",
                    color = LoanLabColors.Text2,
                    style = LabelText.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatIndian(totalPayment),
                    color = LoanLabColors.Text1,
                    style = HeroNumber.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
fun ChartLegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = LoanLabColors.ChartBlue, label = "Principal")
        Spacer(modifier = Modifier.weight(1f))
        LegendItem(color = LoanLabColors.ChartRed, label = "Interest")
        Spacer(modifier = Modifier.weight(1f))
        LegendItem(color = LoanLabColors.ChartGreen, label = "Prepayment")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = LoanLabColors.Text2, style = BodySecond)
    }
}
