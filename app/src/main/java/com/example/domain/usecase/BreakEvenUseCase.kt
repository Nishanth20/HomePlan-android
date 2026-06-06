package com.example.domain.usecase

import com.example.domain.model.LoanInput
import kotlin.math.max

data class BreakEvenResult(
    val bestMonth: Int,
    val monthLabel: String,
    val finalSavings: Double,
    val possible: Boolean
)

class BreakEvenUseCase(private val simulateUseCase: SimulateLoanUseCase) {

    fun execute(currentInput: LoanInput, targetSaving: Double, prepayAmount: Double): BreakEvenResult {
        val (_, originalSummary) = try {
            simulateUseCase.execute(currentInput)
        } catch (e: Exception) {
            return BreakEvenResult(0, "", 0.0, false)
        }
        
        var latestMonth = -1
        var latestMonthLabel = ""
        var achievedSavings = 0.0
        
        // Loop up to the original months used to find the latest month that meets target interest saving.
        // Prepayments made earlier save more interest.
        for (m in 1..originalSummary.monthsUsed) {
            val existingLump = currentInput.lumpSumEntries[m] ?: 0.0
            val updatedLumpSumEntries = currentInput.lumpSumEntries.toMutableMap().apply {
                put(m, existingLump + prepayAmount)
            }
            val modifiedInput = currentInput.copy(lumpSumEntries = updatedLumpSumEntries)
            val (rows, modifiedSummary) = try {
                simulateUseCase.execute(modifiedInput)
            } catch (e: Exception) {
                continue
            }
            
            val savings = simulateUseCase.round2(originalSummary.totalInterest - modifiedSummary.totalInterest)
            if (savings >= targetSaving) {
                latestMonth = m
                achievedSavings = savings
                latestMonthLabel = if (m <= rows.size) rows[m - 1].monthLabel else "Month $m"
            } else {
                // Since interest savings strictly decrease as prepayment is delayed,
                // once we drop below the targetSaving, no further m will exceed it.
                break
            }
        }
        
        return if (latestMonth != -1) {
            BreakEvenResult(
                bestMonth = latestMonth,
                monthLabel = latestMonthLabel,
                finalSavings = achievedSavings,
                possible = true
            )
        } else {
            BreakEvenResult(
                bestMonth = 0,
                monthLabel = "",
                finalSavings = 0.0,
                possible = false
            )
        }
    }
}
