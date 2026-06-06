package com.example.domain.usecase

import com.example.domain.model.AnnualSummaryRow
import com.example.domain.model.MonthRow

class BuildAnnualSummaryUseCase(private val simulateUseCase: SimulateLoanUseCase) {

    fun execute(monthRows: List<MonthRow>): List<AnnualSummaryRow> {
        if (monthRows.isEmpty()) return emptyList()

        val grouped = monthRows.groupBy { ((it.monthNo - 1) / 12) + 1 }
        return grouped.map { (yearNo, rows) ->
            val startRow = rows.first()
            val endRow = rows.last()

            AnnualSummaryRow(
                yearNo = yearNo,
                startMonthLabel = startRow.monthLabel,
                endMonthLabel = endRow.monthLabel,
                emiTotal = simulateUseCase.round2(rows.sumOf { it.emiPaid }),
                interestTotal = simulateUseCase.round2(rows.sumOf { it.interest }),
                principalTotal = simulateUseCase.round2(rows.sumOf { it.principal }),
                prepaymentTotal = simulateUseCase.round2(rows.sumOf { it.prepaymentApplied }),
                closingBalance = endRow.closingBalance,
                tax24bTotal = simulateUseCase.round2(rows.sumOf { it.sec24bEligible }),
                tax80cTotal = simulateUseCase.round2(rows.sumOf { it.sec80cEligible })
            )
        }
    }
}
