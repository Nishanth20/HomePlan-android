package com.example.domain.usecase

import com.example.domain.model.LoanInput
import com.example.domain.model.PrepaymentMode
import com.example.domain.model.ScenarioResult

class ScenarioCompareUseCase(private val simulateUseCase: SimulateLoanUseCase) {

    fun execute(input: LoanInput): List<ScenarioResult> {
        // S1: Base Loan — no prepayments, REDUCE_TENURE, original rateChanges
        val s1Input = input.copy(
            mode = PrepaymentMode.REDUCE_TENURE,
            lumpSumEntries = emptyMap(),
            extraEmiEntries = emptyMap()
        )
        val (_, s1Summary) = simulateUseCase.execute(s1Input)

        // S2: Your Plan EMI — lumpSumEntries + extraEmiEntries, REDUCE_EMI, original rateChanges
        val s2Input = input.copy(
            mode = PrepaymentMode.REDUCE_EMI
        )
        val (_, s2Summary) = try {
            simulateUseCase.execute(s2Input)
        } catch (e: LoanSimulationException) {
            Pair(emptyList(), s1Summary) // fallback if fails, but usually we just want to run successfully
        }

        // S3: Your Plan Tenure — lumpSumEntries + extraEmiEntries, REDUCE_TENURE, original rateChanges
        val s3Input = input.copy(
            mode = PrepaymentMode.REDUCE_TENURE
        )
        val (_, s3Summary) = try {
            simulateUseCase.execute(s3Input)
        } catch (e: LoanSimulationException) {
            Pair(emptyList(), s1Summary)
        }

        // S4: Rate Stress +2% — lumpSumEntries + extraEmiEntries, REDUCE_TENURE, all rateChanges +2%
        val stressedRateChanges = input.rateChanges.mapValues { it.value + 2.0 }
        val s4Input = input.copy(
            annualRate = input.annualRate + 2.0,
            mode = PrepaymentMode.REDUCE_TENURE,
            rateChanges = stressedRateChanges
        )
        val (_, s4Summary) = try {
            simulateUseCase.execute(s4Input)
        } catch (e: LoanSimulationException) {
            Pair(emptyList(), s1Summary)
        }

        return listOf(
            ScenarioResult(
                scenarioName = "Base Loan",
                monthsUsed = s1Summary.monthsUsed,
                totalInterest = s1Summary.totalInterest,
                totalPrepayment = s1Summary.totalPrepayment,
                interestSaved = 0.0,
                prepaymentTotal = s1Summary.totalPrepayment
            ),
            ScenarioResult(
                scenarioName = "Your Plan (REDUCE_EMI)",
                monthsUsed = s2Summary.monthsUsed,
                totalInterest = s2Summary.totalInterest,
                totalPrepayment = s2Summary.totalPrepayment,
                interestSaved = simulateUseCase.round2(s1Summary.totalInterest - s2Summary.totalInterest),
                prepaymentTotal = s2Summary.totalPrepayment
            ),
            ScenarioResult(
                scenarioName = "Your Plan (REDUCE_TENURE)",
                monthsUsed = s3Summary.monthsUsed,
                totalInterest = s3Summary.totalInterest,
                totalPrepayment = s3Summary.totalPrepayment,
                interestSaved = simulateUseCase.round2(s1Summary.totalInterest - s3Summary.totalInterest),
                prepaymentTotal = s3Summary.totalPrepayment
            ),
            ScenarioResult(
                scenarioName = "Rate Stress +2%",
                monthsUsed = s4Summary.monthsUsed,
                totalInterest = s4Summary.totalInterest,
                totalPrepayment = s4Summary.totalPrepayment,
                interestSaved = simulateUseCase.round2(s1Summary.totalInterest - s4Summary.totalInterest),
                prepaymentTotal = s4Summary.totalPrepayment
            )
        )
    }
}
