package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

object LoanLabColors {
    val Background    = Color(0xFF0C0C0F)   // near-black with warm undertone
    val Surface       = Color(0xFF141418)   // card surface
    val SurfaceHigh   = Color(0xFF1C1C22)   // elevated element
    val SurfaceBorder = Color(0xFF2A2A35)   // subtle border

    val Accent        = Color(0xFF5E6AD2)   // primary indigo
    val AccentSoft    = Color(0x135E6AD2)   // indigo at ~7% — backgrounds
    val AccentMid     = Color(0x2B5E6AD2)   // indigo at ~17% — selected state

    val Positive      = Color(0xFF3DB87A)   // green for savings
    val PositiveSoft  = Color(0x133DB87A)
    val Warning       = Color(0xFFD4860B)   // amber for stress
    val WarningSoft   = Color(0x13D4860B)
    val Negative      = Color(0xFFD95F5F)   // red for cost/danger
    val NegativeSoft  = Color(0x13D95F5F)

    val Text1         = Color(0xFFF2F2F7)   // primary text
    val Text2         = Color(0xFF8E8E9A)   // secondary labels
    val Text3         = Color(0xFF56565F)   // disabled / muted
    val TextInverse   = Color(0xFF0C0C0F)   // on colored backgrounds

    val ChartBlue     = Color(0xFF5E6AD2)
    val ChartGreen    = Color(0xFF3DB87A)
    val ChartRed      = Color(0xFFD95F5F)
    val ChartAmber    = Color(0xFFD4860B)
}

val HeroGradient = Brush.linearGradient(listOf(Color(0xFF3A3D8F), Color(0xFF5E6AD2)))
val SavingsGradient = Brush.linearGradient(listOf(Color(0xFF2E724F), Color(0xFF3DB87A)))
val WarningGradient = Brush.linearGradient(listOf(Color(0xFF905E15), Color(0xFFD4860B)))
val NegativeGradient = Brush.linearGradient(listOf(Color(0xFF913D3D), Color(0xFFD95F5F)))
