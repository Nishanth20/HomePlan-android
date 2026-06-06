package com.example.presentation.screens.annual

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AnnualSummaryRow
import com.example.presentation.components.AnnualStackedBarChart
import com.example.presentation.components.LegendItem
import com.example.presentation.components.formatIndian
import com.example.ui.theme.*

@Composable
fun AnnualSummaryScreen(
    viewModel: AnnualSummaryViewModel,
    modifier: Modifier = Modifier
) {
    val annualRows by viewModel.annualRows.collectAsState()
    val input by viewModel.input.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoanLabColors.Background)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LoanLabColors.Accent)
            }
        } else if (annualRows.isEmpty() || input == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Setup active loan profile first to review annual analytics.", color = LoanLabColors.Text3, style = BodyPrimary)
            }
        } else {
            val totalYears = annualRows.size
            val totalInterest = annualRows.sumOf { it.interestTotal }
            val totalTaxBenefit = annualRows.sumOf { it.tax24bTotal + it.tax80cTotal }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Annual Outflows",
                    style = DisplayTitle,
                    color = LoanLabColors.Text1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Quick stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LoanLabColors.Surface)
                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("TOTAL YEARS", color = LoanLabColors.Text2, style = TinyText)
                        Text("$totalYears Yrs", color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = BodyPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                        Text("TOTAL INTEREST", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(totalInterest), color = LoanLabColors.Negative, fontWeight = FontWeight.Bold, style = BodyPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                        Text("TAX BENEFITS", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(totalTaxBenefit), color = LoanLabColors.Positive, fontWeight = FontWeight.Bold, style = BodyPrimary)
                    }
                }

                // Years list
                Text("YEARLY DETAIL BREAKDOWNS", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    annualRows.forEach { row ->
                        AnnualYearRowItem(row = row)
                    }
                }

                // Stacked Bar Chart
                Text("ANNUAL STACKED DISTRIBUTION BREAKDOWN", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LoanLabColors.Surface)
                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    ) {
                        Box(modifier = Modifier.width(maxOf(300.dp, (annualRows.size * 44).dp))) {
                            AnnualStackedBarChart(
                                rows = annualRows,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Legend row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = LoanLabColors.ChartBlue, label = "Principal")
                    Spacer(modifier = Modifier.weight(1f))
                    LegendItem(color = LoanLabColors.ChartRed, label = "Interest")
                }
            }
        }
    }
}

@Composable
fun AnnualYearRowItem(row: AnnualSummaryRow) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LoanLabColors.Surface)
            .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "Year ${row.yearNo}",
                    color = LoanLabColors.Accent,
                    fontWeight = FontWeight.Bold,
                    style = BodyPrimary
                )
                Text(
                    text = "${row.startMonthLabel} \u2192 ${row.endMonthLabel}",
                    color = LoanLabColors.Text2,
                    style = TinyText
                )
            }
            Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.End) {
                Text("Total EMI", color = LoanLabColors.Text2, style = TinyText)
                Text(formatIndian(row.emiTotal), color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = LabelText)
            }
            Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.End) {
                Text("Prepaid", color = LoanLabColors.Text2, style = TinyText)
                Text(
                    text = if (row.prepaymentTotal > 0.05) formatIndian(row.prepaymentTotal) else "\u20B90",
                    color = if (row.prepaymentTotal > 0.05) LoanLabColors.Positive else LoanLabColors.Text2,
                    fontWeight = FontWeight.Bold,
                    style = LabelText
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LoanLabColors.SurfaceHigh)
                    .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Detailed Outflow", color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = LabelText, modifier = Modifier.padding(bottom = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Interest Total", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(row.interestTotal), color = LoanLabColors.Negative, fontWeight = FontWeight.Bold, style = LabelText)
                    }
                    Column {
                        Text("Principal Total", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(row.principalTotal), color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = LabelText)
                    }
                    Column {
                        Text("Closing Balance", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(row.closingBalance), color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = LabelText)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(0.08f))
                Spacer(modifier = Modifier.height(8.dp))

                Text("Tax Benefits Claimed", color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = LabelText, modifier = Modifier.padding(bottom = 6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Sec 24b (Interest)", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(row.tax24bTotal), color = LoanLabColors.Positive, fontWeight = FontWeight.Bold, style = LabelText)
                    }
                    Column {
                        Text("Sec 80C (Principal)", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(row.tax80cTotal), color = LoanLabColors.Positive, fontWeight = FontWeight.Bold, style = LabelText)
                    }
                }
            }
        }
    }
}
