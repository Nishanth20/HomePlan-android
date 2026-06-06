package com.example.domain.model

data class AnnualSummaryRow(
    val yearNo: Int,
    val startMonthLabel: String,
    val endMonthLabel: String,
    val emiTotal: Double,
    val interestTotal: Double,
    val principalTotal: Double,
    val prepaymentTotal: Double,
    val closingBalance: Double,
    val tax24bTotal: Double,
    val tax80cTotal: Double
)
