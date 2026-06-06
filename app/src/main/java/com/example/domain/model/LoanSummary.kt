package com.example.domain.model

data class LoanSummary(
    val totalInterest: Double,
    val totalPayment: Double,
    val monthsUsed: Int,
    val totalPrepayment: Double,
    val finalEmi: Double,
    val tax24bTotal: Double,
    val tax80cTotal: Double
)
