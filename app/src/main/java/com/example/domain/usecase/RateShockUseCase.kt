package com.example.domain.usecase

import com.example.domain.model.LoanInput

data class RateShockResult(
    val originalMonths: Int,
    val newMonths: Int,
    val originalInterest: Double,
    val newInterest: Double,
    val extraInterestCost: Double,
    val newEmiAfterMonthN: Double
)

class RateShockUseCase(private val simulateUseCase: SimulateLoanUseCase) {

    fun execute(currentInput: LoanInput, monthN: Int, newRateX: Double): RateShockResult {
        val (originalRows, originalSummary) = simulateUseCase.execute(currentInput)

        val updatedRateChanges = currentInput.rateChanges.toMutableMap().apply {
            put(monthN, newRateX)
        }
        val modifiedInput = currentInput.copy(rateChanges = updatedRateChanges)
        val (modifiedRows, modifiedSummary) = simulateUseCase.execute(modifiedInput)

        val newEmi = modifiedRows.find { it.monthNo == monthN }?.emiPaid
            ?: modifiedRows.lastOrNull()?.emiPaid
            ?: 0.0

        return RateShockResult(
            originalMonths = originalSummary.monthsUsed,
            newMonths = modifiedSummary.monthsUsed,
            originalInterest = originalSummary.totalInterest,
            newInterest = modifiedSummary.totalInterest,
            extraInterestCost = simulateUseCase.round2(modifiedSummary.totalInterest - originalSummary.totalInterest),
            newEmiAfterMonthN = newEmi
        )
    }
}
