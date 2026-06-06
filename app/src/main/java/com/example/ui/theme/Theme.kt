package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LoanLabColorScheme = darkColorScheme(
    primary = LoanLabColors.Accent,
    background = LoanLabColors.Background,
    surface = LoanLabColors.Surface,
    surfaceVariant = LoanLabColors.SurfaceHigh,
    onBackground = LoanLabColors.Text1,
    onSurface = LoanLabColors.Text1,
    onSurfaceVariant = LoanLabColors.Text2,
    outline = LoanLabColors.SurfaceBorder,
    error = LoanLabColors.Negative
)

@Composable
fun LoanLabTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LoanLabColorScheme,
        typography = Typography,
        content = content
    )
}
