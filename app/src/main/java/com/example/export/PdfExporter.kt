package com.example.export

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import androidx.core.content.FileProvider
import com.example.domain.model.AnnualSummaryRow
import com.example.domain.model.LoanInput
import com.example.domain.model.LoanSummary
import com.example.domain.model.MonthRow
import com.example.domain.model.ScenarioResult
import com.example.presentation.components.formatIndian
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.Date

class PdfExporter {

    fun generatePdfFile(
        context: Context,
        input: LoanInput,
        months: List<MonthRow>,
        summary: LoanSummary,
        scenarios: List<ScenarioResult>,
        annualRows: List<AnnualSummaryRow>
    ): File {
        val cachePath = File(context.cacheDir, "exports")
        cachePath.mkdirs()
        val file = File(cachePath, "LoanLab_Report_${System.currentTimeMillis()}.pdf")
        
        val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
        try {
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            // Header Font
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(37, 68, 65))
            val sectionFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor(124, 58, 237))
            val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.DARK_GRAY)
            val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor.DARK_GRAY)
            val headerCellFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)

            // Header Banner
            val titleParagraph = Paragraph("LoanLab India — Home Loan Report", titleFont)
            titleParagraph.alignment = Element.ALIGN_CENTER
            titleParagraph.spacingAfter = 20f
            document.add(titleParagraph)

            // Date
            val dateLabel = Paragraph("Generated on: " + Date().toString(), normalFont)
            dateLabel.alignment = Element.ALIGN_RIGHT
            dateLabel.spacingAfter = 10f
            document.add(dateLabel)

            // Page 1: Loan Summary
            document.add(Paragraph("1. Loan Parameters & Savings Summary", sectionFont))
            document.add(Paragraph(" ", normalFont))

            val summaryTable = PdfPTable(2)
            summaryTable.widthPercentage = 100f
            summaryTable.setWidths(floatArrayOf(1.5f, 2f))

            addTableCell(summaryTable, "Loan Principal", boldFont, isHeader = true)
            addTableCell(summaryTable, formatIndian(input.principal), normalFont)
            addTableCell(summaryTable, "Disbursement Date", boldFont, isHeader = true)
            addTableCell(summaryTable, input.startDate.toString(), normalFont)
            addTableCell(summaryTable, "Annual Interest Rate", boldFont, isHeader = true)
            addTableCell(summaryTable, "${input.annualRate}%", normalFont)
            addTableCell(summaryTable, "Scheduled Tenure (Years)", boldFont, isHeader = true)
            addTableCell(summaryTable, "${input.tenureYears} Years", normalFont)
            addTableCell(summaryTable, "Prepayment Mode", boldFont, isHeader = true)
            addTableCell(summaryTable, input.mode.name, normalFont)
            addTableCell(summaryTable, "Total Interest Paid", boldFont, isHeader = true)
            addTableCell(summaryTable, formatIndian(summary.totalInterest), normalFont)
            addTableCell(summaryTable, "Total Prepayments Applied", boldFont, isHeader = true)
            addTableCell(summaryTable, formatIndian(summary.totalPrepayment), normalFont)
            addTableCell(summaryTable, "Total Outflow (Principal + Interest)", boldFont, isHeader = true)
            addTableCell(summaryTable, formatIndian(summary.totalPayment), normalFont)
            addTableCell(summaryTable, "Sec 24b Benefit Used", boldFont, isHeader = true)
            addTableCell(summaryTable, formatIndian(summary.tax24bTotal), normalFont)
            addTableCell(summaryTable, "Sec 80C Benefit Used", boldFont, isHeader = true)
            addTableCell(summaryTable, formatIndian(summary.tax80cTotal), normalFont)

            document.add(summaryTable)
            document.newPage()

            // Page 2: Scenario Comparison
            document.add(Paragraph("2. Strategic Scenario Comparison", sectionFont))
            document.add(Paragraph(" ", normalFont))

            val scenTable = PdfPTable(5)
            scenTable.widthPercentage = 100f
            scenTable.setWidths(floatArrayOf(1.5f, 1f, 1f, 1f, 1f))

            addTableHeaderCell(scenTable, "Scenario Name", headerCellFont)
            addTableHeaderCell(scenTable, "Months Used", headerCellFont)
            addTableHeaderCell(scenTable, "Total Interest", headerCellFont)
            addTableHeaderCell(scenTable, "Prepayments", headerCellFont)
            addTableHeaderCell(scenTable, "Interest Saved", headerCellFont)

            for (s in scenarios) {
                addTableCell(scenTable, s.scenarioName, boldFont)
                addTableCell(scenTable, s.monthsUsed.toString(), normalFont)
                addTableCell(scenTable, formatIndian(s.totalInterest), normalFont)
                addTableCell(scenTable, formatIndian(s.totalPrepayment), normalFont)
                addTableCell(scenTable, formatIndian(s.interestSaved), normalFont)
            }
            document.add(scenTable)
            document.newPage()

            // Page 3: Annual Summary
            document.add(Paragraph("3. Annual Outflow Breakdown", sectionFont))
            document.add(Paragraph(" ", normalFont))

            val annTable = PdfPTable(6)
            annTable.widthPercentage = 100f
            annTable.setWidths(floatArrayOf(0.6f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f))

            addTableHeaderCell(annTable, "Year", headerCellFont)
            addTableHeaderCell(annTable, "EMI Total", headerCellFont)
            addTableHeaderCell(annTable, "Interest Paid", headerCellFont)
            addTableHeaderCell(annTable, "Principal Paid", headerCellFont)
            addTableHeaderCell(annTable, "Prepayments", headerCellFont)
            addTableHeaderCell(annTable, "Closing Balance", headerCellFont)

            for (row in annualRows) {
                addTableCell(annTable, "Y${row.yearNo}", boldFont)
                addTableCell(annTable, formatIndian(row.emiTotal), normalFont)
                addTableCell(annTable, formatIndian(row.interestTotal), normalFont)
                addTableCell(annTable, formatIndian(row.principalTotal), normalFont)
                addTableCell(annTable, formatIndian(row.prepaymentTotal), normalFont)
                addTableCell(annTable, formatIndian(row.closingBalance), normalFont)
            }
            document.add(annTable)
            document.newPage()

            // Appendix: Monthly Schedule (paginated, e.g., 28 rows per page)
            document.add(Paragraph("Appendix: Month-by-Month Amortization Table", sectionFont))
            document.add(Paragraph(" ", normalFont))

            var scheduleTable = createScheduleTable(headerCellFont)
            var count = 0
            for (m in months) {
                if (count > 0 && count % 28 == 0) {
                    document.add(scheduleTable)
                    document.newPage()
                    document.add(Paragraph("Appendix: Month-by-Month Amortization Table (Cont.)", sectionFont))
                    document.add(Paragraph(" ", normalFont))
                    scheduleTable = createScheduleTable(headerCellFont)
                }
                addTableCell(scheduleTable, m.monthNo.toString(), normalFont)
                addTableCell(scheduleTable, m.monthLabel, normalFont)
                addTableCell(scheduleTable, formatIndian(m.openingBalance), normalFont)
                addTableCell(scheduleTable, "${m.annualRate}%", normalFont)
                addTableCell(scheduleTable, formatIndian(m.emiPaid), normalFont)
                addTableCell(scheduleTable, formatIndian(m.interest), normalFont)
                addTableCell(scheduleTable, formatIndian(m.principal), normalFont)
                addTableCell(scheduleTable, formatIndian(m.prepaymentApplied), normalFont)
                addTableCell(scheduleTable, formatIndian(m.closingBalance), normalFont)
                count++
            }
            if (count % 28 != 0) {
                document.add(scheduleTable)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
        return file
    }

    fun generateExcelPdfFile(
        context: Context,
        months: List<MonthRow>
    ): File {
        val cachePath = File(context.cacheDir, "exports")
        cachePath.mkdirs()
        val file = File(cachePath, "LoanLab_ExcelPrint_${System.currentTimeMillis()}.pdf")
        
        val document = Document(PageSize.A4.rotate(), 20f, 20f, 30f, 20f) // landscape for clean excel sheet style
        try {
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor(50, 50, 50))
            val cellFont = Font(Font.FontFamily.COURIER, 8f, Font.NORMAL, BaseColor.DARK_GRAY)
            val headerFont = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor.WHITE)

            val titleParagraph = Paragraph("EXCEL EXPORT WORKBOOK - AMORTIZATION SCHEDULE", titleFont)
            titleParagraph.spacingAfter = 15f
            document.add(titleParagraph)

            var table = createExcelScheduleTable(headerFont)
            var count = 0
            for (m in months) {
                if (count > 0 && count % 38 == 0) {
                    document.add(table)
                    document.newPage()
                    document.add(Paragraph("EXCEL EXPORT WORKBOOK - AMORTIZATION SCHEDULE (CONT.)", titleFont))
                    document.add(Paragraph(" ", cellFont))
                    table = createExcelScheduleTable(headerFont)
                }
                addExcelCell(table, m.monthNo.toString(), cellFont)
                addExcelCell(table, m.monthLabel, cellFont)
                addExcelCell(table, String.format("%.2f", m.openingBalance), cellFont)
                addExcelCell(table, "${m.annualRate}%", cellFont)
                addExcelCell(table, String.format("%.2f", m.emiPaid), cellFont)
                addExcelCell(table, String.format("%.2f", m.interest), cellFont)
                addExcelCell(table, String.format("%.2f", m.principal), cellFont)
                addExcelCell(table, String.format("%.2f", m.prepaymentApplied), cellFont)
                addExcelCell(table, String.format("%.2f", m.closingBalance), cellFont)
                addExcelCell(table, m.remainingMonths.toString(), cellFont)
                addExcelCell(table, String.format("%.2f", m.nextEmi), cellFont)
                count++
            }
            document.add(table)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
        return file
    }

    fun exportAndShare(
        context: Context,
        input: LoanInput,
        months: List<MonthRow>,
        summary: LoanSummary,
        scenarios: List<ScenarioResult>,
        annualRows: List<AnnualSummaryRow>
    ) {
        val file = generatePdfFile(context, input, months, summary, scenarios, annualRows)
        shareFile(context, file, "application/pdf", "LoanLab India - Home Loan PDF Report")
    }

    fun exportAndPrint(
        context: Context,
        input: LoanInput,
        months: List<MonthRow>,
        summary: LoanSummary,
        scenarios: List<ScenarioResult>,
        annualRows: List<AnnualSummaryRow>
    ) {
        val file = generatePdfFile(context, input, months, summary, scenarios, annualRows)
        printPdf(context, file)
    }

    fun exportAndPrintExcel(
        context: Context,
        months: List<MonthRow>
    ) {
        val file = generateExcelPdfFile(context, months)
        printPdf(context, file)
    }

    fun printPdf(context: Context, file: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
        if (printManager != null) {
            val jobName = file.name
            val printAdapter = PrintFileAdapter(file)
            printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
        }
    }

    private fun createScheduleTable(font: Font): PdfPTable {
        val t = PdfPTable(9)
        t.widthPercentage = 100f
        t.setWidths(floatArrayOf(0.4f, 0.8f, 1.1f, 0.6f, 1.1f, 1f, 1f, 1f, 1.1f))
        addTableHeaderCell(t, "Mo", font)
        addTableHeaderCell(t, "Month", font)
        addTableHeaderCell(t, "Opening", font)
        addTableHeaderCell(t, "Rate", font)
        addTableHeaderCell(t, "EMI Paid", font)
        addTableHeaderCell(t, "Interest", font)
        addTableHeaderCell(t, "Principal", font)
        addTableHeaderCell(t, "Prepay", font)
        addTableHeaderCell(t, "Closing", font)
        return t
    }

    private fun createExcelScheduleTable(font: Font): PdfPTable {
        val t = PdfPTable(11)
        t.widthPercentage = 100f
        t.setWidths(floatArrayOf(0.4f, 0.7f, 1.1f, 0.6f, 1.0f, 1.0f, 1.0f, 1.0f, 1.1f, 0.6f, 1.0f))
        addExcelHeaderCell(t, "Mo", font)
        addExcelHeaderCell(t, "Month", font)
        addExcelHeaderCell(t, "Opening Bal", font)
        addExcelHeaderCell(t, "Rate", font)
        addExcelHeaderCell(t, "EMI Paid", font)
        addExcelHeaderCell(t, "Interest", font)
        addExcelHeaderCell(t, "Principal", font)
        addExcelHeaderCell(t, "Prepayment", font)
        addExcelHeaderCell(t, "Closing Bal", font)
        addExcelHeaderCell(t, "Rem Mo", font)
        addExcelHeaderCell(t, "Next EMI", font)
        return t
    }

    private fun addTableHeaderCell(table: PdfPTable, text: String, font: Font) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = BaseColor(37, 68, 65)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(6f)
        table.addCell(cell)
    }

    private fun addExcelHeaderCell(table: PdfPTable, text: String, font: Font) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = BaseColor(20, 110, 60) // Excel Theme Dark Green
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(4f)
        table.addCell(cell)
    }

    private fun addTableCell(table: PdfPTable, text: String, font: Font, isHeader: Boolean = false) {
        val cell = PdfPCell(Phrase(text, font))
        if (isHeader) {
            cell.backgroundColor = BaseColor(243, 248, 252)
        }
        cell.setPadding(5f)
        table.addCell(cell)
    }

    private fun addExcelCell(table: PdfPTable, text: String, font: Font) {
        val cell = PdfPCell(Phrase(text, font))
        cell.setPadding(3f)
        table.addCell(cell)
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

class PrintFileAdapter(private val file: File) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        var input: FileInputStream? = null
        var output: FileOutputStream? = null

        try {
            input = FileInputStream(file)
            output = FileOutputStream(destination?.fileDescriptor)

            val buf = ByteArray(16384)
            var size: Int
            while (input.read(buf).also { size = it } >= 0) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }
                output.write(buf, 0, size)
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.toString())
        } finally {
            try {
                input?.close()
                output?.close()
            } catch (e: IOException) {
                // ignore
            }
        }
    }
}
