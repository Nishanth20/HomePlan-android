package com.example.presentation.screens.scenarios

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.domain.model.ScenarioResult
import com.example.presentation.components.ScenarioBarChart
import com.example.presentation.components.formatIndian
import com.example.ui.theme.*

@Composable
fun ScenariosScreen(
    viewModel: ScenariosViewModel,
    modifier: Modifier = Modifier
) {
    val scenarios by viewModel.scenarios.collectAsState()
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
        } else if (scenarios.isEmpty() || input == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Setup active loan profile first to run scenario analyzer.", color = LoanLabColors.Text3, style = BodyPrimary)
            }
        } else {
            val bestScenario = remember(scenarios) {
                scenarios.minByOrNull { it.totalInterest }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Which strategy saves the most?",
                    style = DisplayTitle,
                    color = LoanLabColors.Text1,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Compare savings results across distinct adjustment streams and rate stress simulations.",
                    style = BodySecond,
                    color = LoanLabColors.Text2,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Render 4 scenario cards (no staggered delays to respect "remove stagger" instruction)
                scenarios.forEach { s ->
                    val isBest = s == bestScenario
                    ScenarioCardContent(
                        s = s,
                        isBest = isBest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart 3 Section
                Text("TOTAL LIFETIME INTEREST BY SCENARIO", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LoanLabColors.Surface)
                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    ScenarioBarChart(
                        scenarios = scenarios,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Advisory Section
                Text("STRATEGIC SCENARIOS ADVISORY", style = TinyText, fontWeight = FontWeight.Bold, color = LoanLabColors.Text3, modifier = Modifier.padding(bottom = 8.dp))
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
                        AdvisoryNoteItem(
                            title = "Base Loan (Benchmark)",
                            msg = "Represents regular scheduled outgo with no early repayments. Serves as your absolute interest comparison line.",
                            color = LoanLabColors.Accent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdvisoryNoteItem(
                            title = "Your Plan (REDUCE_EMI)",
                            msg = "Repays lumps early to drop mandatory monthly EMI checks instantly. Perfect for ensuring persistent monthly cashflow flexibility.",
                            color = LoanLabColors.Accent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdvisoryNoteItem(
                            title = "Your Plan (REDUCE_TENURE)",
                            msg = "Maintains scheduled EMIs while crushing outstanding months. Standard way to trigger massive compound savings.",
                            color = LoanLabColors.Positive
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdvisoryNoteItem(
                            title = "Rate Stress +2%",
                            msg = "Simulates floating rate increases of +2% cumulative. Serves as a financial stress buffer to plan ahead.",
                            color = LoanLabColors.Warning
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScenarioCardContent(
    s: ScenarioResult,
    isBest: Boolean,
    modifier: Modifier = Modifier
) {
    val titleColor = when {
        s.scenarioName.contains("Base") -> LoanLabColors.Text2
        s.scenarioName.contains("REDUCE_EMI") -> LoanLabColors.Accent
        s.scenarioName.contains("REDUCE_TENURE") -> LoanLabColors.Positive
        else -> LoanLabColors.Warning
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isBest) LoanLabColors.Positive.copy(alpha = 0.5f) else LoanLabColors.SurfaceBorder,
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = LoanLabColors.Surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = s.scenarioName,
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    style = BodyPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isBest) {
                    Box(
                        modifier = Modifier
                            .background(LoanLabColors.PositiveSoft, RoundedCornerShape(12.dp))
                            .border(1.dp, LoanLabColors.Positive, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("✓ Best Plan", color = LoanLabColors.Positive, style = TinyText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Duration", color = LoanLabColors.Text2, style = TinyText)
                    Text("${s.monthsUsed} mos (${String.format("%.1f", s.monthsUsed / 12.0)} yrs)", color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = LabelText)
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Total Interest", color = LoanLabColors.Text2, style = TinyText)
                    Text(formatIndian(s.totalInterest), color = LoanLabColors.Text1, fontWeight = FontWeight.Bold, style = LabelText)
                }
                Column(modifier = Modifier.weight(1f)) {
                    // BUG 4 FIX: If interest Saved is negative, label is Extra Cost in Red, else Interest Saved in Green
                    val saved = s.interestSaved
                    val label = if (saved >= 0.0) "You Save" else "Extra Cost"
                    val valueColor = if (saved >= 0.0) LoanLabColors.Positive else LoanLabColors.Negative
                    val valueText = formatIndian(kotlin.math.abs(saved))

                    Text(label, color = LoanLabColors.Text2, style = TinyText)
                    Text(
                        text = valueText,
                        color = valueColor,
                        fontWeight = FontWeight.Bold,
                        style = LabelText
                    )
                }
            }
        }
    }
}

@Composable
fun AdvisoryNoteItem(title: String, msg: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = color, fontWeight = FontWeight.Bold, style = BodySecond)
            Text(msg, color = LoanLabColors.Text2, style = TinyText, lineHeight = 15.sp)
        }
    }
}
