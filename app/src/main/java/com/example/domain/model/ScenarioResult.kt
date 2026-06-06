package com.example.domain.model

data class ScenarioResult(
    val scenarioName: String,
    val monthsUsed: Int,
    val totalInterest: Double,
    val totalPrepayment: Double,
    val interestSaved: Double,
    val prepaymentTotal: Double
)
