package com.example.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LoanLabColors
import com.example.ui.theme.DisplayTitle
import com.example.ui.theme.SectionTitle
import com.example.ui.theme.BodyPrimary
import com.example.ui.theme.TinyText

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        OnboardingStep(
            title = "Welcome to LoanLab India",
            description = "Your premium companion designed specifically for Indian home loans and floating interest rates. Take absolute control of your debt.",
            icon = Icons.Default.Home,
            iconTint = LoanLabColors.Accent
        ),
        OnboardingStep(
            title = "Dual-Advisor Intelligence",
            description = "Should you reduce your EMI or shorten your tenure? Compare both prepayment paths side-by-side to discover the strategy that saves you lakhs.",
            icon = Icons.Default.Star,
            iconTint = LoanLabColors.Positive
        ),
        OnboardingStep(
            title = "Prepare for Rate Shocks",
            description = "Stay ahead of RBI interest rate fluctuations. Input expected floating rate changes over the years to stress-test your mortgage before they impact your wallet.",
            icon = Icons.Default.Warning,
            iconTint = LoanLabColors.Warning
        ),
        OnboardingStep(
            title = "Accurate Tax Simulations",
            description = "Calculate dynamic annual deductions accurately. Track Section 24b (Interest) and Section 80C (Principal) tax caps under real Indian regulatory ceilings.",
            icon = Icons.Default.Info,
            iconTint = LoanLabColors.Accent
        ),
        OnboardingStep(
            title = "Operational Command",
            description = "Your local, uncompressed offline-first cockpit is fully calibrated. Absolutely zero data ever leaves your device.",
            icon = Icons.Default.CheckCircle,
            iconTint = LoanLabColors.Positive
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoanLabColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.4f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                contentAlignment = Alignment.Center
            ) {
                val step = steps[currentStep]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(step.iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = step.iconTint,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = step.title,
                        style = DisplayTitle,
                        color = LoanLabColors.Text1,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.description,
                        style = BodyPrimary,
                        color = LoanLabColors.Text2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    steps.forEachIndexed { idx, _ ->
                        val active = idx == currentStep
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(if (active) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(if (active) LoanLabColors.Accent else LoanLabColors.SurfaceBorder)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        TextButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.testTag("onboarding_back")
                        ) {
                            Text(
                                text = "Back",
                                style = SectionTitle.copy(fontSize = 15.sp),
                                color = LoanLabColors.Text2
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    if (currentStep < steps.lastIndex) {
                        Button(
                            onClick = { currentStep++ },
                            colors = ButtonDefaults.buttonColors(containerColor = LoanLabColors.Accent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("onboarding_next")
                        ) {
                            Text(
                                text = "Next",
                                style = SectionTitle.copy(fontSize = 15.sp),
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.completeOnboarding {
                                    onFinish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LoanLabColors.Positive),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("onboarding_finish")
                        ) {
                            Text(
                                text = "Enter Cockpit",
                                style = SectionTitle.copy(fontSize = 15.sp),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color
)
