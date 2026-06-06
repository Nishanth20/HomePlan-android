package com.example.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.domain.model.AnnualSummaryRow
import com.example.domain.model.LoanInput
import com.example.domain.model.LoanSummary
import com.example.domain.model.MonthRow
import com.example.domain.model.ScenarioResult
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CsvExporter {

    fun exportAndShare(
        context: Context,
        input: LoanInput,
        months: List<MonthRow>,
        summary: LoanSummary,
        scenarios: List<ScenarioResult>,
        annualRows: List<AnnualSummaryRow>
    ) {
        val cachePath = File(context.cacheDir, "exports")
        cachePath.mkdirs()
        
        // Define temporary CSV file paths
        val summaryFile = File(cachePath, "loan_summary.csv")
        val scheduleFile = File(cachePath, "monthly_schedule.csv")
        val annualFile = File(cachePath, "annual_summary.csv")
        val scenarioFile = File(cachePath, "scenario_compare.csv")
        
        // 1. Create loan_summary.csv
        summaryFile.writeText(buildString {
            append("Field,Value\n")
            append("Loan Amount (Principal),${input.principal}\n")
            append("Disbursement Date,${input.startDate}\n")
            append("Annual Rate,${input.annualRate}\n")
            append("Tenure Years,${input.tenureYears}\n")
            append("Adjustment Mode,${input.mode.name}\n")
            append("Total Interest Paid,${summary.totalInterest}\n")
            append("Total Prepayments Applied,${summary.totalPrepayment}\n")
            append("Total Payment,${summary.totalPayment}\n")
            append("Sec 24b Benefit,${summary.tax24bTotal}\n")
            append("Sec 80c Benefit,${summary.tax80cTotal}\n")
        })

        // 2. Create monthly_schedule.csv
        scheduleFile.writeText(buildString {
            append("Month No,Month,Opening Balance,Annual Rate,EMI Paid,Interest,Principal,Lump Sum Prepayment,Extra EMI Count,Extra EMI Amt,Prepayment Applied,Closing Balance,Remaining Months,Next EMI,Total EMIs In Loan Year,Sec 24b Interest,Sec 80c Principal\n")
            for (m in months) {
                append("${m.monthNo},${m.monthLabel},${m.openingBalance},${m.annualRate},${m.emiPaid},${m.interest},${m.principal},${m.lumpSum},${m.extraEmiCount},${m.extraEmiAmount},${m.prepaymentApplied},${m.closingBalance},${m.remainingMonths},${m.nextEmi},${m.totalEmisInLoanYear},${m.sec24bEligible},${m.sec80cEligible}\n")
            }
        })

        // 3. Create annual_summary.csv
        annualFile.writeText(buildString {
            append("Year,Start Month,End Month,EMI Total,Interest Total,Principal Total,Prepayment Total,Closing Balance,Sec 24b Benefit Total,Sec 80c Benefit Total\n")
            for (row in annualRows) {
                append("${row.yearNo},${row.startMonthLabel},${row.endMonthLabel},${row.emiTotal},${row.interestTotal},${row.principalTotal},${row.prepaymentTotal},${row.closingBalance},${row.tax24bTotal},${row.tax80cTotal}\n")
            }
        })

        // 4. Create scenario_compare.csv
        scenarioFile.writeText(buildString {
            append("Scenario Name,Payoff Months,Payoff Years,Total Interest,Total Prepayments,Interest Saved vs Base\n")
            for (s in scenarios) {
                append("${s.scenarioName},${s.monthsUsed},${String.format("%.2f", s.monthsUsed / 12.0)},${s.totalInterest},${s.totalPrepayment},${s.interestSaved}\n")
            }
        })

        // Zip all files together
        val zipFile = File(cachePath, "LoanLab_CSV_Export_${System.currentTimeMillis()}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            addFileToZip(zos, summaryFile, "loan_summary.csv")
            addFileToZip(zos, scheduleFile, "monthly_schedule.csv")
            addFileToZip(zos, annualFile, "annual_summary.csv")
            addFileToZip(zos, scenarioFile, "scenario_compare.csv")
        }

        // Delete temporary csv files
        summaryFile.delete()
        scheduleFile.delete()
        annualFile.delete()
        scenarioFile.delete()

        // Share Excel-compatible zip
        shareFile(context, zipFile, "application/zip", "LoanLab India - Excel CSV Report")
    }

    fun exportSingleExcelCsv(
        context: Context,
        months: List<MonthRow>
    ) {
        val cachePath = File(context.cacheDir, "exports")
        cachePath.mkdirs()
        val file = File(cachePath, "LoanLab_Amortization_Excel_${System.currentTimeMillis()}.csv")

        file.writeText(buildString {
            append("Month No,Month,Opening Balance,Annual Rate,EMI Paid,Interest,Principal,Lump Sum Prepayment,Extra EMI Count,Extra EMI Amt,Prepayment Applied,Closing Balance,Remaining Months,Next EMI\n")
            for (m in months) {
                append("${m.monthNo},${m.monthLabel},${m.openingBalance},${m.annualRate},${m.emiPaid},${m.interest},${m.principal},${m.lumpSum},${m.extraEmiCount},${m.extraEmiAmount},${m.prepaymentApplied},${m.closingBalance},${m.remainingMonths},${m.nextEmi}\n")
            }
        })

        shareFile(context, file, "text/comma-separated-values", "LoanLab India - Excel Spreadsheet")
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, zipEntryName: String) {
        val entry = ZipEntry(zipEntryName)
        zos.putNextEntry(entry)
        zos.write(file.readBytes())
        zos.closeEntry()
    }

    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
