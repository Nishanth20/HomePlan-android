package com.example.presentation.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LoanLabColors
import com.example.ui.theme.BodySecond
import com.example.ui.theme.LabelText

@Composable
fun AnimatedCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    format: (Double) -> String
) {
    var animValue by remember { mutableStateOf(0.0) }
    LaunchedEffect(targetValue) {
        animate(
            initialValue = 0f,
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = 1000,
                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
            )
        ) { valCurrent, _ ->
            animValue = valCurrent.toDouble()
        }
    }
    Text(
        text = format(animValue),
        style = style,
        modifier = modifier
    )
}

@Composable
fun KpiCard(
    labelText: String,
    value: Double,
    format: (Double) -> String,
    modifier: Modifier = Modifier,
    gradient: Brush? = null,
    testTag: String = "",
    useHighlight: Boolean = false
) {
    val entryScale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "entry"
    )

    val borderColor = if (useHighlight) LoanLabColors.Positive.copy(alpha = 0.5f) else LoanLabColors.SurfaceBorder
    val valueColor = if (useHighlight) LoanLabColors.Positive else LoanLabColors.Text1

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(entryScale)
            .clip(RoundedCornerShape(16.dp))
            .background(LoanLabColors.Surface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = labelText,
                color = LoanLabColors.Text2,
                style = TinyTextHelper()
            )
            Spacer(modifier = Modifier.height(6.dp))
            AnimatedCounter(
                targetValue = value,
                format = format,
                style = TextStyle(
                    color = valueColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(LoanLabColors.Surface)
            .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(cornerRadius))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun TinyTextHelper(): TextStyle {
    return TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    )
}
