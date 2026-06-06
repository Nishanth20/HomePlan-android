package com.example.presentation.screens.input

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LoanProfile
import com.example.domain.model.PrepaymentMode
import com.example.presentation.components.InfoDialog
import com.example.presentation.components.formatIndian
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val principal by viewModel.principal.collectAsState()
    val rate by viewModel.rate.collectAsState()
    val tenure by viewModel.tenure.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val startDate by viewModel.startDate.collectAsState()

    val lumpSums by viewModel.lumpSums.collectAsState()
    val extraEmis by viewModel.extraEmis.collectAsState()
    val rateChanges by viewModel.rateChanges.collectAsState()
    val profiles by viewModel.savedProfiles.collectAsState()

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var datePickedFor by remember { mutableStateOf<String?>(null) }
    var showProfileSaveDialog by remember { mutableStateOf(false) }
    var profileNameInput by remember { mutableStateOf("") }

    var lumpExpand by remember { mutableStateOf(false) }
    var extraExpand by remember { mutableStateOf(false) }
    var rateExpand by remember { mutableStateOf(false) }
    var profilesExpand by remember { mutableStateOf(false) }

    var lumpMonth by remember { mutableStateOf("") }
    var lumpAmount by remember { mutableStateOf("") }
    var extraMonth by remember { mutableStateOf("") }
    var extraCount by remember { mutableStateOf("") }
    var rateMonth by remember { mutableStateOf("") }
    var rateNewValue by remember { mutableStateOf("") }

    // Info dialogue state
    var currentInfoTitle by remember { mutableStateOf<String?>(null) }
    var currentInfoContent by remember { mutableStateOf<String?>(null) }

    val shakeOffset = remember { Animatable(0f) }
    
    val triggerShake: suspend () -> Unit = {
        repeat(3) {
            shakeOffset.animateTo(12f, spring(stiffness = Spring.StiffnessHigh))
            shakeOffset.animateTo(-12f, spring(stiffness = Spring.StiffnessHigh))
        }
        shakeOffset.animateTo(0f)
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { msg ->
            scope.launch {
                triggerShake()
                snackbarHostState.showSnackbar(msg)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoanLabColors.Background)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Plan Your Loan",
                    style = DisplayTitle,
                    color = LoanLabColors.Text1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Perform high-precision simulation based on floating resets and tax reductions.",
                    style = BodySecond,
                    color = LoanLabColors.Text2,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Main Core Input Fields
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shakeOffset.value.toInt(), 0) }
                        .clip(RoundedCornerShape(16.dp))
                        .background(LoanLabColors.Surface)
                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        // Principal field
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Loan Amount (\u20B9)", style = LabelText, color = LoanLabColors.Text1)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    currentInfoTitle = "Loan Amount (Principal)"
                                    currentInfoContent = "The principal sum borrowed from the bank. Must be positive."
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "info", tint = LoanLabColors.Text3, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = principal,
                            onValueChange = { viewModel.updatePrincipal(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("principal_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            supportingText = {
                                val dVal = principal.toDoubleOrNull() ?: 0.0
                                Text("Real-time Format: ${formatIndian(dVal)}", color = LoanLabColors.Accent, style = TinyText)
                            },
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rate field
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Annual Interest Rate (%)", style = LabelText, color = LoanLabColors.Text1)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    currentInfoTitle = "Interest Rate"
                                    currentInfoContent = "The nominal floating rate for repayment. Resets are applied according to specific calendar logs."
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "info", tint = LoanLabColors.Text3, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = rate,
                            onValueChange = { viewModel.updateRate(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rate_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tenure field
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tenure (Years)", style = LabelText, color = LoanLabColors.Text1)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    currentInfoTitle = "Tenure"
                                    currentInfoContent = "Repayment timeline in years. Usually up to 30 years."
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "info", tint = LoanLabColors.Text3, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = tenure,
                            onValueChange = { viewModel.updateTenure(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tenure_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Prepayment Mode Segmented Select
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Prepayment Mode", style = LabelText, color = LoanLabColors.Text2)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    currentInfoTitle = "Prepayment Mode"
                                    currentInfoContent = "REDUCE TENURE keeps the EMI amount identical and shortens the loan duration. REDUCE EMI recalibrates your debt to immediately lower your mandatory monthly liabilities while keeping the original timeline."
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "info", tint = LoanLabColors.Text3, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(LoanLabColors.SurfaceHigh)
                                .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(14.dp))
                                .padding(4.dp)
                        ) {
                            listOf(PrepaymentMode.REDUCE_TENURE, PrepaymentMode.REDUCE_EMI).forEach { m ->
                                val active = mode == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) LoanLabColors.Accent else Color.Transparent)
                                        .clickable { viewModel.updateMode(m) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (m == PrepaymentMode.REDUCE_TENURE) "Reduce Tenure" else "Reduce EMI",
                                        fontWeight = FontWeight.SemiBold,
                                        style = LabelText,
                                        color = if (active) Color.White else LoanLabColors.Text2
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date Pick Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LoanLabColors.SurfaceHigh)
                                .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable { showDatePicker = true }
                               .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "calendar",
                                tint = LoanLabColors.Accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("First EMI Month", color = LoanLabColors.Text2, style = TinyText)
                                Text(startDate.toString(), color = LoanLabColors.Text1, style = LabelText, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "arrow",
                                tint = LoanLabColors.Text3,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expanding Accordion 1: LUMP SUM
                ExpandableInputSection(
                    title = "Lump Sum Prepayments",
                    expanded = lumpExpand,
                    onToggle = { lumpExpand = !lumpExpand },
                    badgeCount = lumpSums.size
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LoanLabColors.Surface)
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = lumpMonth,
                                onValueChange = { lumpMonth = it },
                                label = { Text("Mo No") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    IconButton(onClick = { datePickedFor = "lump" }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Pick Date",
                                            tint = LoanLabColors.Accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LoanLabColors.Accent,
                                    unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                                    focusedContainerColor = LoanLabColors.SurfaceHigh,
                                    unfocusedContainerColor = LoanLabColors.SurfaceHigh
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = lumpAmount,
                                onValueChange = { lumpAmount = it },
                                label = { Text("Amt (\u20B9)") },
                                modifier = Modifier.weight(1.5f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LoanLabColors.Accent,
                                    unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                                    focusedContainerColor = LoanLabColors.SurfaceHigh,
                                    unfocusedContainerColor = LoanLabColors.SurfaceHigh
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val m = lumpMonth.toIntOrNull() ?: 0
                                    val a = lumpAmount.toDoubleOrNull() ?: 0.0
                                    if (m > 0 && a > 0) {
                                        viewModel.addLumpSum(m, a)
                                        lumpMonth = ""
                                        lumpAmount = ""
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .background(LoanLabColors.Accent, shape = RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            }
                        }

                        // List of entered lumps
                        lumpSums.forEach { (mo, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Month $mo: ", color = LoanLabColors.Text2, style = BodySecond, fontWeight = FontWeight.Bold)
                                Text(formatIndian(amt), color = LoanLabColors.Positive, style = BodySecond, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.removeLumpSum(mo) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "delete", tint = LoanLabColors.Negative, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expanding Accordion 2: EXTRA EMI PLAN
                ExpandableInputSection(
                    title = "Extra EMI Plan",
                    expanded = extraExpand,
                    onToggle = { extraExpand = !extraExpand },
                    badgeCount = extraEmis.size
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LoanLabColors.Surface)
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = extraMonth,
                                onValueChange = { extraMonth = it },
                                label = { Text("Mo No") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    IconButton(onClick = { datePickedFor = "extra" }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Pick Date",
                                            tint = LoanLabColors.Accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LoanLabColors.Accent,
                                    unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                                    focusedContainerColor = LoanLabColors.SurfaceHigh,
                                    unfocusedContainerColor = LoanLabColors.SurfaceHigh
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = extraCount,
                                onValueChange = { extraCount = it },
                                label = { Text("ExtraCount") },
                                modifier = Modifier.weight(1.5f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LoanLabColors.Accent,
                                    unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                                    focusedContainerColor = LoanLabColors.SurfaceHigh,
                                    unfocusedContainerColor = LoanLabColors.SurfaceHigh
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val m = extraMonth.toIntOrNull() ?: 0
                                    val c = extraCount.toIntOrNull() ?: 0
                                    if (m > 0 && c > 0) {
                                        viewModel.addExtraEmi(m, c)
                                        extraMonth = ""
                                        extraCount = ""
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .background(LoanLabColors.Accent, shape = RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            }
                        }

                        // List of extra Emis
                        extraEmis.forEach { (mo, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Month $mo: ", color = LoanLabColors.Text2, style = BodySecond, fontWeight = FontWeight.Bold)
                                Text("$count Extra EMIs", color = LoanLabColors.Accent, style = BodySecond, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.removeExtraEmi(mo) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "delete", tint = LoanLabColors.Negative, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expanding Accordion 3: RATE CHANGES
                ExpandableInputSection(
                    title = "Interest Rate Changes",
                    expanded = rateExpand,
                    onToggle = { rateExpand = !rateExpand },
                    badgeCount = rateChanges.size
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LoanLabColors.Surface)
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = rateMonth,
                                onValueChange = { rateMonth = it },
                                label = { Text("Mo No") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    IconButton(onClick = { datePickedFor = "rate" }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Pick Date",
                                            tint = LoanLabColors.Accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LoanLabColors.Accent,
                                    unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                                    focusedContainerColor = LoanLabColors.SurfaceHigh,
                                    unfocusedContainerColor = LoanLabColors.SurfaceHigh
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = rateNewValue,
                                onValueChange = { rateNewValue = it },
                                label = { Text("New Rate (%)") },
                                modifier = Modifier.weight(1.5f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LoanLabColors.Accent,
                                    unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                                    focusedContainerColor = LoanLabColors.SurfaceHigh,
                                    unfocusedContainerColor = LoanLabColors.SurfaceHigh
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val m = rateMonth.toIntOrNull() ?: 0
                                    val r = rateNewValue.toDoubleOrNull() ?: 0.0
                                    if (m > 0 && r >= 0.0) {
                                        viewModel.addRateChange(m, r)
                                        rateMonth = ""
                                        rateNewValue = ""
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .background(LoanLabColors.Accent, shape = RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            }
                        }

                        // List of rate changes
                        rateChanges.forEach { (mo, newR) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Month $mo: ", color = LoanLabColors.Text2, style = BodySecond, fontWeight = FontWeight.Bold)
                                Text("$newR% Interest Rate", color = LoanLabColors.Accent, style = BodySecond, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.removeRateChange(mo) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "delete", tint = LoanLabColors.Negative, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Accordion 4: Saved Loans Profiles
                ExpandableInputSection(
                    title = "Saved Loan Profiles",
                    expanded = profilesExpand,
                    onToggle = { profilesExpand = !profilesExpand },
                    badgeCount = profiles.size
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LoanLabColors.Surface)
                            .padding(16.dp)
                    ) {
                        if (profiles.isEmpty()) {
                            Text("No saved profiles yet. Create your active loan above and hit Save Profile.", color = LoanLabColors.Text3, style = BodySecond)
                        } else {
                            profiles.forEach { profile ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LoanLabColors.SurfaceHigh)
                                        .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.loadProfile(profile) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(profile.name, color = LoanLabColors.Text1, style = BodyPrimary, fontWeight = FontWeight.Bold)
                                        Text("${formatIndian(profile.input.principal)} @ ${profile.input.annualRate}%", color = LoanLabColors.Text2, style = TinyText)
                                    }
                                    IconButton(onClick = { viewModel.deleteProfile(profile.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "delete", tint = LoanLabColors.Negative, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CTA Action Buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (viewModel.buildInput() != null) {
                                showProfileSaveDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LoanLabColors.SurfaceHigh),
                        shape = RoundedCornerShape(12.dp),
                        border = borderStroke(1.dp, LoanLabColors.SurfaceBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("save_profile_btn")
                    ) {
                        Text("Save Profile", style = SectionTitle.copy(fontSize = 14.sp), color = LoanLabColors.Text1)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            viewModel.saveLastUsed()
                            val parsed = viewModel.buildInput()
                            if (parsed != null) {
                                onCalculate()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LoanLabColors.Accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.8f)
                            .height(52.dp)
                            .testTag("calculate_btn")
                    ) {
                        Text("Calculate LEDGER", style = SectionTitle.copy(fontSize = 14.sp), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        // Help Modal dialog
        if (currentInfoTitle != null && currentInfoContent != null) {
            InfoDialog(
                title = currentInfoTitle!!,
                content = currentInfoContent!!,
                onDismiss = {
                    currentInfoTitle = null
                    currentInfoContent = null
                }
            )
        }

        // Material3 Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateStartDate(
                                java.time.Instant.ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                            )
                        }
                        showDatePicker = false
                    }) {
                        Text("Confirm", color = LoanLabColors.Accent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = LoanLabColors.Text2)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Auto-calculate Month Number picker
        if (datePickedFor != null) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { datePickedFor = null },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val pickedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            val monthsDiff = (pickedDate.year - startDate.year) * 12 + (pickedDate.monthValue - startDate.monthValue)
                            val calculatedMonthNo = maxOf(1, monthsDiff + 1)
                            
                            when (datePickedFor) {
                                "lump" -> lumpMonth = calculatedMonthNo.toString()
                                "extra" -> extraMonth = calculatedMonthNo.toString()
                                "rate" -> rateMonth = calculatedMonthNo.toString()
                            }
                        }
                        datePickedFor = null
                    }) {
                        Text("Confirm", color = LoanLabColors.Accent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { datePickedFor = null }) {
                        Text("Cancel", color = LoanLabColors.Text2)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Save Profile Name Dialog
        if (showProfileSaveDialog) {
            AlertDialog(
                onDismissRequest = { showProfileSaveDialog = false },
                title = { Text("Save Loan Profile", color = LoanLabColors.Text1, style = SectionTitle) },
                text = {
                    Column {
                        Text("Provide a descriptive name (e.g. SBI Home Loan 2026)", color = LoanLabColors.Text2, style = BodySecond)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = profileNameInput,
                            onValueChange = { profileNameInput = it },
                            label = { Text("Profile Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LoanLabColors.Accent,
                                unfocusedBorderColor = LoanLabColors.SurfaceBorder,
                                focusedContainerColor = LoanLabColors.SurfaceHigh,
                                unfocusedContainerColor = LoanLabColors.SurfaceHigh
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (profileNameInput.isNotBlank()) {
                            viewModel.saveProfile(profileNameInput)
                            profileNameInput = ""
                            showProfileSaveDialog = false
                        }
                    }) {
                        Text("Save", color = LoanLabColors.Accent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProfileSaveDialog = false }) {
                        Text("Cancel", color = LoanLabColors.Text2)
                    }
                }
            )
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun ExpandableInputSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    badgeCount: Int = 0,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, LoanLabColors.SurfaceBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = LoanLabColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = LoanLabColors.Text1,
                    style = BodyPrimary
                )
                if (badgeCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(LoanLabColors.Accent, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            style = TinyText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowForward,
                    contentDescription = "Expand Indicator",
                    tint = LoanLabColors.Text2,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                content()
            }
        }
    }
}
