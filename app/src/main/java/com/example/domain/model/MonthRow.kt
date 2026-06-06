package com.example.domain.model

data class MonthRow(
    val monthNo: Int,
    val monthLabel: String,
    val openingBalance: Double,
    val annualRate: Double,
    val emiPaid: Double,
    val interest: Double,
    val principal: Double,
    val lumpSum: Double,
    val extraEmiCount: Int,
    val extraEmiAmount: Double,
    val prepaymentApplied: Double,
    val closingBalance: Double,
    val remainingMonths: Int,
    val nextEmi: Double,
    val totalEmisInLoanYear: Int,
    val sec24bEligible: Double,
    val sec80cEligible: Double
)
