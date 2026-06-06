package com.example.presentation.components

import java.util.Locale

fun formatIndian(value: Double, includeDecimals: Boolean = false): String {
    // Handle negative values safely
    val isNegative = value < 0
    val absValue = kotlin.math.abs(value)
    
    val rounded = kotlin.math.round(absValue * 100.0) / 100.0
    val longVal = rounded.toLong()
    val dec = kotlin.math.round((rounded - longVal) * 100).toInt()

    val valStr = longVal.toString()
    val formattedInt = if (valStr.length <= 3) {
        valStr
    } else {
        val last3 = valStr.substring(valStr.length - 3)
        var remaining = valStr.substring(0, valStr.length - 3)
        val segments = mutableListOf<String>()
        while (remaining.length > 2) {
            segments.add(0, remaining.substring(remaining.length - 2))
            remaining = remaining.substring(0, remaining.length - 2)
        }
        if (remaining.isNotEmpty()) {
            segments.add(0, remaining)
        }
        segments.joinToString(",") + "," + last3
    }

    val prefix = if (isNegative) "-\u20B9" else "\u20B9"
    return if (includeDecimals) {
        val decimalsStr = String.format(Locale.US, "%02d", kotlin.math.abs(dec))
        "$prefix$formattedInt.$decimalsStr"
    } else {
        "$prefix$formattedInt"
    }
}
