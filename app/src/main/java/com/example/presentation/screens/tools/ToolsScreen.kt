package com.example.presentation.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.KpiCard
import com.example.presentation.components.formatIndian
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.input.collectAsState()
    val prepayAmount by viewModel.prepayAmount.collectAsState()
    val prepayMonth by viewModel.prepayMonth.collectAsState()
    val rateShockDelta by viewModel.rateShockDelta.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoanLabColors.Background)
    ) {
        if (input == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Setup active loan profile first to use tools.", color = LoanLabColors.Text3, style = BodyPrimary)
            }
        } else {
            val loanInput = input!!
            val maxPrepayAmt = loanInput.principal
            val maxPrepayMonths = (loanInput.tenureYears * 12).toInt()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Financial Stress Lab",
                    style = DisplayTitle,
                    color = LoanLabColors.Text1,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Perform what-if calculations on early prepayments and interest rate fluctuation cycles.",
                    style = BodySecond,
                    color = LoanLabColors.Text2,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // TOOL 1: PREPAYMENT IMPACT CALCULATOR
                Text("SINGLE PREPAYMENT SANDBOX", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LoanLabColors.Surface)
                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Prepayment Amount", color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = BodyPrimary)
                        Text(formatIndian(prepayAmount), color = LoanLabColors.Positive, style = SectionTitle, fontWeight = FontWeight.Bold)

                        Slider(
                            value = prepayAmount.toFloat(),
                            onValueChange = { viewModel.updatePrepayAmount(it.toDouble()) },
                            valueRange = 10000f..maxPrepayAmt.toFloat(),
                            steps = 99,
                            colors = SliderDefaults.colors(
                                thumbColor = LoanLabColors.Positive,
                                activeTrackColor = LoanLabColors.Positive,
                                inactiveTrackColor = LoanLabColors.SurfaceHigh
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Prepayment Timeline", color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = BodyPrimary)
                        Text("Month $prepayMonth (Year ${(prepayMonth / 12.0).roundToInt()})", color = LoanLabColors.Accent, style = SectionTitle, fontWeight = FontWeight.Bold)

                        Slider(
                            value = prepayMonth.toFloat(),
                            onValueChange = { viewModel.updatePrepayMonth(it.roundToInt()) },
                            valueRange = 1f..maxPrepayMonths.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = LoanLabColors.Accent,
                                activeTrackColor = LoanLabColors.Accent,
                                inactiveTrackColor = LoanLabColors.SurfaceHigh
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("SIMULATED BENEFIT RESULTS", color = LoanLabColors.Text3, style = TinyText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            KpiCard(
                                labelText = "Interest Saved",
                                value = viewModel.prepayInterestSaved,
                                format = { formatIndian(it) },
                                modifier = Modifier.weight(1.2f),
                                useHighlight = viewModel.prepayInterestSaved > 0.0
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            KpiCard(
                                labelText = "Months Shaved",
                                value = viewModel.prepayMonthsSaved.toDouble(),
                                format = { "${it.toInt()} Mos" },
                                modifier = Modifier.weight(1f),
                                useHighlight = viewModel.prepayMonthsSaved > 0
                            )
                        }
                    }
                }

                // TOOL 2: RATE SHOCK STRESS TEST
                Text("INTEREST RATE SHOCK SIMULATION", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LoanLabColors.Surface)
                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        val sign = if (rateShockDelta >= 0) "+" else ""
                        val isOver = rateShockDelta > 0.0
                        Text("Floating Rate Deviation", color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = BodyPrimary)
                        Text("$sign${String.format("%.2f", rateShockDelta)}%", color = if (isOver) LoanLabColors.Warning else LoanLabColors.Positive, style = SectionTitle, fontWeight = FontWeight.Bold)

                        Slider(
                            value = rateShockDelta.toFloat(),
                            onValueChange = { viewModel.updateRateShock(it.toDouble()) },
                            valueRange = -3.0f..3.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isOver) LoanLabColors.Warning else LoanLabColors.Positive,
                                activeTrackColor = if (isOver) LoanLabColors.Warning else LoanLabColors.Positive,
                                inactiveTrackColor = LoanLabColors.SurfaceHigh
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("SIMULATED INSTANT STRESS IMPACT", color = LoanLabColors.Text3, style = TinyText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            KpiCard(
                                labelText = "Deviated Monthly EMI",
                                value = viewModel.newEmi,
                                format = { formatIndian(it, includeDecimals = true) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            KpiCard(
                                labelText = "Monthly Outflow Diff",
                                value = viewModel.emiDelta,
                                format = { formatIndian(it, includeDecimals = true) },
                                modifier = Modifier.weight(1f),
                                useHighlight = isOver
                            )
                        }
                        
                        KpiCard(
                            labelText = "Overpaid Aggregate Interest (Stress Liability)",
                            value = viewModel.totalInterestDelta,
                            format = { formatIndian(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
