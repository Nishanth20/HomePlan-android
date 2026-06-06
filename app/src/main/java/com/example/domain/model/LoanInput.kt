package com.example.domain.model

import java.time.LocalDate

data class LoanInput(
    val principal: Double,
    val annualRate: Double,
    val tenureYears: Double,
    val mode: PrepaymentMode,
    val startDate: LocalDate,
    val lumpSumEntries: Map<Int, Double>,
    val extraEmiEntries: Map<Int, Int>,
    val rateChanges: Map<Int, Double>
)
