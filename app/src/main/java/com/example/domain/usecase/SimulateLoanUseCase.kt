package com.example.domain.usecase

import com.example.domain.model.LoanInput
import com.example.domain.model.LoanSummary
import com.example.domain.model.MonthRow
import com.example.domain.model.PrepaymentMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class LoanSimulationException(message: String) : Exception(message)

class SimulateLoanUseCase {

    fun execute(input: LoanInput): Pair<List<MonthRow>, LoanSummary> {
        val totalMonths = max(1, (input.tenureYears * 12).toInt())
        var annualRate = input.annualRate
        var monthlyRate = annualRate / 1200.0
        
        val originalEmi = round2(calcEmi(input.principal, monthlyRate, totalMonths))
        var currentEmi = originalEmi
        var balance = input.principal
        
        val monthRows = mutableListOf<MonthRow>()
        var totalInterest = 0.0
        var totalPayment = 0.0
        var totalPrepayment = 0.0
        var tax24bTotal = 0.0
        var tax80cTotal = 0.0
        
        var annual24bUsed = 0.0
        var annual80cUsed = 0.0
        
        var monthNo = 0
        val dateFormatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.US)
        
        while (balance > 0.005 && monthNo < totalMonths + 600) {
            monthNo++
            
            // Annual cap reset (at the start of every 12-month loan period)
            if ((monthNo - 1) % 12 == 0) {
                annual24bUsed = 0.0
                annual80cUsed = 0.0
            }
            
            // Rate changes
            if (input.rateChanges.containsKey(monthNo)) {
                annualRate = input.rateChanges[monthNo] ?: annualRate
                monthlyRate = annualRate / 1200.0
                if (input.mode == PrepaymentMode.REDUCE_EMI) {
                    val remaining = totalMonths - monthNo + 1
                    if (remaining > 0 && balance > 0.005) {
                        currentEmi = round2(calcEmi(balance, monthlyRate, remaining))
                    }
                }
            }
            
            val openingBalance = balance
            val interestAmt = round2(openingBalance * monthlyRate)
            
            var emiPaid = if (input.mode == PrepaymentMode.REDUCE_TENURE) originalEmi else currentEmi
            
            if (emiPaid <= interestAmt && balance > 0.005) {
                throw LoanSimulationException("EMI of \u20B9$emiPaid is too low to cover interest of \u20B9$interestAmt in month $monthNo. Increase EMI or lower the rate changes to proceed.")
            }
            
            var principalAmt = round2(emiPaid - interestAmt)
            if (principalAmt > balance) {
                principalAmt = balance
                emiPaid = round2(interestAmt + principalAmt)
            }
            
            val lumpAmt = input.lumpSumEntries[monthNo] ?: 0.0
            val extraCount = input.extraEmiEntries[monthNo] ?: 0
            val extraAmt = round2(extraCount * emiPaid)
            
            val allowedPrepayment = round2(max(0.0, balance - principalAmt))
            val prepaymentApplied = round2(min(allowedPrepayment, lumpAmt + extraAmt))
            
            val closingBalance = round2(max(0.0, balance - principalAmt - prepaymentApplied))
            val remainingMonths = max(0, totalMonths - monthNo)
            
            val nextEmi = when {
                closingBalance <= 0.005 -> 0.0
                input.mode == PrepaymentMode.REDUCE_TENURE -> originalEmi
                remainingMonths > 0 -> round2(calcEmi(closingBalance, monthlyRate, remainingMonths))
                else -> 0.0
            }
            
            // Calculate extra EMIs in this specific year
            val currentYear = ((monthNo - 1) / 12) + 1
            val yStarts = (currentYear - 1) * 12 + 1
            val yEnds = currentYear * 12
            var extraEmisInThisYear = 0
            for (m in yStarts..yEnds) {
                extraEmisInThisYear += input.extraEmiEntries[m] ?: 0
            }
            val totalEmisInLoanYear = 12 + extraEmisInThisYear
            
            // Real Indian tax laws allow claiming benefits up to the annual caps on a yearly basis
            // with no sub-limits on monthly distributions.
            val sec24bEligible = round2(min(interestAmt, max(0.0, 200000.0 - annual24bUsed)))
            val sec80cEligible = round2(min(principalAmt, max(0.0, 150000.0 - annual80cUsed)))
            
            annual24bUsed += sec24bEligible
            annual80cUsed += sec80cEligible
            
            val rowDate = input.startDate.plusMonths((monthNo - 1).toLong())
            val monthLabel = rowDate.format(dateFormatter)
            
            monthRows.add(
                MonthRow(
                    monthNo = monthNo,
                    monthLabel = monthLabel,
                    openingBalance = openingBalance,
                    annualRate = annualRate,
                    emiPaid = emiPaid,
                    interest = interestAmt,
                    principal = principalAmt,
                    lumpSum = lumpAmt,
                    extraEmiCount = extraCount,
                    extraEmiAmount = extraAmt,
                    prepaymentApplied = prepaymentApplied,
                    closingBalance = if (closingBalance < 0.005) 0.0 else closingBalance,
                    remainingMonths = remainingMonths,
                    nextEmi = nextEmi,
                    totalEmisInLoanYear = totalEmisInLoanYear,
                    sec24bEligible = sec24bEligible,
                    sec80cEligible = sec80cEligible
                )
            )
            
            totalInterest += interestAmt
            totalPayment += (emiPaid + prepaymentApplied)
            totalPrepayment += prepaymentApplied
            tax24bTotal += sec24bEligible
            tax80cTotal += sec80cEligible
            
            balance = if (closingBalance < 0.005) 0.0 else closingBalance
            currentEmi = nextEmi
        }
        
        val summary = LoanSummary(
            totalInterest = round2(totalInterest),
            totalPayment = round2(totalPayment),
            monthsUsed = monthNo,
            totalPrepayment = round2(totalPrepayment),
            finalEmi = if (monthRows.isNotEmpty()) monthRows.last().nextEmi else 0.0,
            tax24bTotal = round2(tax24bTotal),
            tax80cTotal = round2(tax80cTotal)
        )
        
        return Pair(monthRows, summary)
    }
    
    fun calcEmi(principal: Double, monthlyRate: Double, totalMonths: Int): Double {
        if (totalMonths <= 0) return 0.0
        if (monthlyRate <= 0.0) return principal / totalMonths
        val factor = (1.0 + monthlyRate).pow(totalMonths.toDouble())
        return principal * monthlyRate * factor / (factor - 1.0)
    }
    
    fun round2(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
    }
}
