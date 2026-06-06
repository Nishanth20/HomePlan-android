package com.example.presentation.screens.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthRow
import com.example.domain.model.PrepaymentMode
import com.example.presentation.components.formatIndian
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.input.collectAsState()
    val filteredList by viewModel.filteredRows.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val listState = rememberLazyListState()
    val filterScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoanLabColors.Background)
    ) {
        if (input == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active loan profile. Setup in tab parameters.", color = LoanLabColors.Text3, textAlign = TextAlign.Center, style = BodyPrimary)
            }
        } else {
            val loanInput = input!!

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Payment Schedule",
                    style = DisplayTitle,
                    color = LoanLabColors.Text1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Row of Sticky parameters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LoanLabColors.Surface)
                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Scheduled EMI", color = LoanLabColors.Text2, style = TinyText)
                        Text(
                            text = if (filteredList.isNotEmpty()) formatIndian(filteredList.first().emiPaid) else "\u20B9--",
                            color = LoanLabColors.Text1,
                            style = LabelText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text("Current Mode", color = LoanLabColors.Text2, style = TinyText)
                        Text(
                            text = if (loanInput.mode == PrepaymentMode.REDUCE_TENURE) "Reduce Tenure" else "Reduce EMI",
                            color = LoanLabColors.Accent,
                            style = LabelText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text("Payoff Date", color = LoanLabColors.Text2, style = TinyText)
                        Text(
                            text = if (filteredList.isNotEmpty()) filteredList.last().monthLabel else "--",
                            color = LoanLabColors.Positive,
                            style = LabelText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search specific month no...", color = LoanLabColors.Text3, style = BodySecond) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search", tint = LoanLabColors.Text2, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LoanLabColors.Text1,
                        unfocusedTextColor = LoanLabColors.Text1,
                        focusedBorderColor = LoanLabColors.Accent,
                        unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                        focusedContainerColor = LoanLabColors.SurfaceHigh,
                        unfocusedContainerColor = LoanLabColors.SurfaceHigh
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Chips Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .horizontalScroll(filterScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScheduleFilter.values().forEach { filter ->
                        val active = currentFilter == filter
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) LoanLabColors.Accent else LoanLabColors.Surface)
                                .border(1.dp, if (active) Color.Transparent else LoanLabColors.SurfaceBorder, RoundedCornerShape(20.dp))
                                .clickable { viewModel.updateFilter(filter) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = when (filter) {
                                    ScheduleFilter.ALL -> "All Months"
                                    ScheduleFilter.PREPAYMENTS_ONLY -> "Prepayments"
                                    ScheduleFilter.YEARS_1_5 -> "Yrs 1-5"
                                    ScheduleFilter.YEARS_6_10 -> "Yrs 6-10"
                                    ScheduleFilter.YEARS_11_PLUS -> "Yrs 11+"
                                },
                                color = if (active) Color.White else LoanLabColors.Text2,
                                style = LabelText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Horizontal scrollable headers for schedule table
                TableHeaderRow()

                Spacer(modifier = Modifier.height(4.dp))

                // List Area
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList, key = { it.monthNo }) { monthRow ->
                        ScheduleRowItem(monthRow = monthRow)
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(LoanLabColors.Surface)
            .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#", modifier = Modifier.width(28.dp), color = LoanLabColors.Text2, style = TinyText, fontWeight = FontWeight.Bold)
        Text("MONTH", modifier = Modifier.width(58.dp), color = LoanLabColors.Text2, style = TinyText, fontWeight = FontWeight.Bold)
        Text("BALANCE", modifier = Modifier.width(76.dp), color = LoanLabColors.Text2, style = TinyText, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("EMI", modifier = Modifier.width(64.dp), color = LoanLabColors.Text2, style = TinyText, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("INTEREST", modifier = Modifier.width(66.dp), color = LoanLabColors.Text2, style = TinyText, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("PRINCIPAL", modifier = Modifier.width(66.dp), color = LoanLabColors.Text2, style = TinyText, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("EXTRA", modifier = Modifier.weight(1f), color = LoanLabColors.Text2, style = TinyText, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
fun ScheduleRowItem(monthRow: MonthRow) {
    var expanded by remember { mutableStateOf(false) }
    val isPrepaid = monthRow.prepaymentApplied > 0.05

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPrepaid) LoanLabColors.PositiveSoft else LoanLabColors.Surface)
            .border(
                1.dp,
                if (isPrepaid) LoanLabColors.Positive.copy(alpha = 0.4f) else LoanLabColors.SurfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${monthRow.monthNo}",
                modifier = Modifier.width(28.dp),
                color = LoanLabColors.Text3,
                style = TinyText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = monthRow.monthLabel,
                modifier = Modifier.width(58.dp),
                color = if (isPrepaid) LoanLabColors.Positive else LoanLabColors.Accent,
                style = TinyText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = formatIndian(monthRow.openingBalance, includeDecimals = false),
                modifier = Modifier.width(76.dp),
                color = LoanLabColors.Text1,
                style = TinyText,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = formatIndian(monthRow.emiPaid, includeDecimals = false),
                modifier = Modifier.width(64.dp),
                color = LoanLabColors.Text1,
                style = TinyText,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = formatIndian(monthRow.interest, includeDecimals = false),
                modifier = Modifier.width(66.dp),
                color = LoanLabColors.Text1,
                style = TinyText,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = formatIndian(monthRow.principal, includeDecimals = false),
                modifier = Modifier.width(66.dp),
                color = LoanLabColors.Text1,
                style = TinyText,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = if (isPrepaid) formatIndian(monthRow.prepaymentApplied, includeDecimals = false) else "₹0",
                modifier = Modifier.weight(1f),
                color = if (isPrepaid) LoanLabColors.Positive else LoanLabColors.Text3,
                style = TinyText,
                textAlign = TextAlign.End,
                fontWeight = if (isPrepaid) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
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
                Text(
                    text = "Financial Details \u2014 Month ${monthRow.monthNo}",
                    color = LoanLabColors.Text1,
                    fontWeight = FontWeight.Bold,
                    style = LabelText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Interest Component", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(monthRow.interest), color = LoanLabColors.Negative, style = LabelText, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Closing Balance", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(monthRow.closingBalance), color = LoanLabColors.Text1, style = LabelText, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Active Rate", color = LoanLabColors.Text2, style = TinyText)
                        Text("${monthRow.annualRate}%", color = LoanLabColors.Accent, style = LabelText, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Sec 24b Interest Tax Benefit", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(monthRow.sec24bEligible), color = LoanLabColors.Positive, style = LabelText, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Sec 80C Principal Tax Benefit", color = LoanLabColors.Text2, style = TinyText)
                        Text(formatIndian(monthRow.sec80cEligible), color = LoanLabColors.Positive, style = LabelText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
