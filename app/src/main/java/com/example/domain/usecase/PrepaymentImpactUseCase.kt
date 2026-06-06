package com.example.domain.usecase

import com.example.domain.model.LoanInput

data class PrepaymentImpactResult(
    val monthsSaved: Int,
    val interestSaved: Double
)

class PrepaymentImpactUseCase(private val simulateUseCase: SimulateLoanUseCase) {

    fun execute(currentInput: LoanInput, monthN: Int, amountX: Double): PrepaymentImpactResult {
        // Run original plan
        val (_, originalSummary) = simulateUseCase.execute(currentInput)

        // Run modified plan
        val existingLump = currentInput.lumpSumEntries[monthN] ?: 0.0
        val updatedLumpSumEntries = currentInput.lumpSumEntries.toMutableMap().apply {
            put(monthN, existingLump + amountX)
        }
        val modifiedInput = currentInput.copy(lumpSumEntries = updatedLumpSumEntries)
        val (_, modifiedSummary) = simulateUseCase.execute(modifiedInput)

        val monthsSaved = originalSummary.monthsUsed - modifiedSummary.monthsUsed
        val interestSaved = simulateUseCase.round2(originalSummary.totalInterest - modifiedSummary.totalInterest)

        return PrepaymentImpactResult(
            monthsSaved = maxOf(0, monthsSaved),
            interestSaved = maxOf(0.0, interestSaved)
        )
    }
}
