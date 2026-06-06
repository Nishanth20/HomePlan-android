package com.example.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PrepaymentMode
import com.example.export.CsvExporter
import com.example.export.PdfExporter
import com.example.presentation.components.*
import com.example.ui.theme.*
import com.example.domain.usecase.ScenarioCompareUseCase
import com.example.domain.usecase.SimulateLoanUseCase
import com.example.domain.usecase.BuildAnnualSummaryUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val input by viewModel.input.collectAsState()
    val monthRows by viewModel.monthRows.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val baseline by viewModel.baseline.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoanLabColors.Background)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LoanLabColors.Accent)
            }
        } else if (input == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "empty",
                    tint = LoanLabColors.Text3,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to LoanLab India",
                    style = DisplayTitle,
                    color = LoanLabColors.Text1,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configure loans to run comprehensive amortization schedules and compare prepayment strategies.",
                    style = BodyPrimary,
                    color = LoanLabColors.Text2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToInput,
                    colors = ButtonDefaults.buttonColors(containerColor = LoanLabColors.Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Configure Loan Ledger", style = SectionTitle, color = Color.White)
                }
            }
        } else {
            val loanInput = input!!
            val loanSummary = summary
            val baseSummary = baseline

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Top Header Row with Export Shortcuts
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LoanLab Dashboard",
                            style = DisplayTitle,
                            color = LoanLabColors.Text1
                        )
                        Text(
                            text = if (loanInput.mode == PrepaymentMode.REDUCE_TENURE) "Active Mode: Reduce Tenure" else "Active Mode: Reduce EMI",
                            style = LabelText,
                            color = LoanLabColors.Accent
                        )
                    }

                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "PDF Options", tint = LoanLabColors.Accent)
                    }
                    
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.List, contentDescription = "Excel Options", tint = LoanLabColors.Positive)
                    }
                }

                if (error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LoanLabColors.NegativeSoft)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Simulation Error", color = LoanLabColors.Negative, style = SectionTitle, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(error!!, color = LoanLabColors.Text1, style = BodySecond)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToInput,
                                colors = ButtonDefaults.buttonColors(containerColor = LoanLabColors.Negative),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Adjust Plan Parameters", style = LabelText, color = Color.White)
                            }
                        }
                    }
                }

                if (loanSummary != null && baseSummary != null) {
                    val yearsSaved = maxOf(0.0, (baseSummary.monthsUsed - loanSummary.monthsUsed) / 12.0)
                    val interestSaved = maxOf(0.0, baseSummary.totalInterest - loanSummary.totalInterest)

                    // Card block: CORE PARAMS
                    Text("Your Loan", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        KpiCard(
                            labelText = "Loan Principal",
                            value = loanInput.principal,
                            format = { formatIndian(it) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        KpiCard(
                            labelText = "Interest Rate",
                            value = loanInput.annualRate,
                            format = { "${String.format("%.2f", it)}%" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        KpiCard(
                            labelText = "Scheduled Tenure",
                            value = loanInput.tenureYears,
                            format = { "${it.toInt()} years" },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        KpiCard(
                            labelText = "Actual Payoff",
                            value = loanSummary.monthsUsed.toDouble(),
                            format = { "${it.toInt()} months" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Card block: ACCUMULATED SAVINGS
                    Text("Your Savings", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        KpiCard(
                            labelText = "Total Interest",
                            value = loanSummary.totalInterest,
                            format = { formatIndian(it) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        KpiCard(
                            labelText = "Prepayments Paid",
                            value = loanSummary.totalPrepayment,
                            format = { formatIndian(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        KpiCard(
                            labelText = "Tenure Shortened",
                            value = yearsSaved,
                            format = { "${String.format("%.2f", it)} years" },
                            modifier = Modifier.weight(1f),
                            useHighlight = yearsSaved > 0.0
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        KpiCard(
                            labelText = "Interest Saved",
                            value = interestSaved,
                            format = { formatIndian(it) },
                            modifier = Modifier.weight(1f),
                            useHighlight = interestSaved > 0.0
                        )
                    }

                    // Wide Tax Card
                    Text("Tax Benefit Estimate", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LoanLabColors.Surface)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Estimated Tax Benefits (24b + 80C)",
                                    style = LabelText,
                                    color = LoanLabColors.Text2
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatIndian(loanSummary.tax24bTotal + loanSummary.tax80cTotal),
                                style = HeroNumber,
                                color = LoanLabColors.Text1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sec 24b: ${formatIndian(loanSummary.tax24bTotal)} | Sec 80C: ${formatIndian(loanSummary.tax80cTotal)}",
                                style = TinyText,
                                color = LoanLabColors.Text2
                            )
                        }
                    }

                    // Chart 1: Area
                    Text("Interest vs Principal", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LoanLabColors.Surface)
                            .padding(12.dp)
                    ) {
                        FintechAreaChart(
                            monthRows = monthRows,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ChartLegendRow()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Chart 2: Donut
                    Text("Where Your Money Goes", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LoanLabColors.Surface)
                            .padding(12.dp)
                    ) {
                        FintechDonutChart(
                            interest = loanSummary.totalInterest,
                            principal = loanInput.principal,
                            prepayment = loanSummary.totalPrepayment,
                            totalPayment = loanSummary.totalPayment,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Strategic Advisory card
                    Text("Advice", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LoanLabColors.Surface)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("What This Means", color = LoanLabColors.Accent, fontWeight = FontWeight.Bold, style = BodyPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when {
                                    yearsSaved >= 2.0 -> "Great job! Your extra payments are reducing your loan term significantly and saving you a lot of interest."
                                    loanSummary.totalPrepayment > 0.0 && yearsSaved < 1.0 -> "Your prepayments are mostly lowering your monthly EMI. Switch to 'Pay Off Faster' mode to save more total interest."
                                    else -> "Even small extra payments in the first few years can save you lakhs in interest. Try to pay a little extra when you can."
                                },
                                color = LoanLabColors.Text2,
                                style = BodySecond
                            )
                        }
                    }
                }
                
                // Advanced Export Options
                if (showExportDialog && loanSummary != null) {
                    AlertDialog(
                        onDismissRequest = { showExportDialog = false },
                        title = {
                            Text(
                                text = "Export & Print Suite",
                                style = SectionTitle,
                                color = LoanLabColors.Text1
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "PDF CHANNELS",
                                    style = TinyText,
                                    color = LoanLabColors.Accent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val simUseCase = SimulateLoanUseCase()
                                            val compUseCase = ScenarioCompareUseCase(simUseCase)
                                            val annUseCase = BuildAnnualSummaryUseCase(simUseCase)
                                            val pdfExp = PdfExporter()
                                            pdfExp.exportAndShare(
                                                context = context,
                                                input = loanInput,
                                                months = monthRows,
                                                summary = loanSummary,
                                                scenarios = compUseCase.execute(loanInput),
                                                annualRows = annUseCase.execute(monthRows)
                                            )
                                            showExportDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = LoanLabColors.Accent, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Share PDF Audit Report", color = LoanLabColors.Text1, fontWeight = FontWeight.SemiBold, style = BodySecond)
                                        Text("Generates standard dynamic audit slip printable", color = LoanLabColors.Text2, style = TinyText)
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val simUseCase = SimulateLoanUseCase()
                                            val compUseCase = ScenarioCompareUseCase(simUseCase)
                                            val annUseCase = BuildAnnualSummaryUseCase(simUseCase)
                                            val pdfExp = PdfExporter()
                                            pdfExp.exportAndPrint(
                                                context = context,
                                                input = loanInput,
                                                months = monthRows,
                                                summary = loanSummary,
                                                scenarios = compUseCase.execute(loanInput),
                                                annualRows = annUseCase.execute(monthRows)
                                            )
                                            showExportDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Print PDF", tint = LoanLabColors.Accent, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Print PDF Document", color = LoanLabColors.Text1, fontWeight = FontWeight.SemiBold, style = BodySecond)
                                        Text("Print layout directly using OS document print manager", color = LoanLabColors.Text2, style = TinyText)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "SPREADSHEET SUITES",
                                    style = TinyText,
                                    color = LoanLabColors.Positive,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val csvExp = CsvExporter()
                                            csvExp.exportSingleExcelCsv(
                                                context = context,
                                                months = monthRows
                                            )
                                            showExportDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.List, contentDescription = "Excel", tint = LoanLabColors.Positive, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Excel Sheet (.csv)", color = LoanLabColors.Text1, fontWeight = FontWeight.SemiBold, style = BodySecond)
                                        Text("Plain tabular spreadsheet readable format", color = LoanLabColors.Text2, style = TinyText)
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val simUseCase = SimulateLoanUseCase()
                                            val compUseCase = ScenarioCompareUseCase(simUseCase)
                                            val annUseCase = BuildAnnualSummaryUseCase(simUseCase)
                                            val csvExp = CsvExporter()
                                            csvExp.exportAndShare(
                                                context = context,
                                                input = loanInput,
                                                months = monthRows,
                                                summary = loanSummary,
                                                scenarios = compUseCase.execute(loanInput),
                                                annualRows = annUseCase.execute(monthRows)
                                            )
                                            showExportDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "All CSV ZIP", tint = LoanLabColors.Positive, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("ZIP Package Workbook", color = LoanLabColors.Text1, fontWeight = FontWeight.SemiBold, style = BodySecond)
                                        Text("All 4 granular tables archived inside a clean zip archive", color = LoanLabColors.Text2, style = TinyText)
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val pdfExp = PdfExporter()
                                            pdfExp.exportAndPrintExcel(
                                                context = context,
                                                months = monthRows
                                            )
                                            showExportDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.List, contentDescription = "Print Grid", tint = LoanLabColors.Positive, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Print Grid Ledger (Landscape)", color = LoanLabColors.Text1, fontWeight = FontWeight.SemiBold, style = BodySecond)
                                        Text("Outputs a ledger-styled table sheet directly", color = LoanLabColors.Text2, style = TinyText)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showExportDialog = false }) {
                                Text("Dismiss", color = LoanLabColors.Text2)
                            }
                        },
                        containerColor = LoanLabColors.SurfaceHigh
                    )
                }
            } // Close else
        }
    }
}
