package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.presentation.ViewModelFactory
import com.example.presentation.screens.about.AboutScreen
import com.example.presentation.screens.annual.AnnualSummaryScreen
import com.example.presentation.screens.annual.AnnualSummaryViewModel
import com.example.presentation.screens.dashboard.DashboardScreen
import com.example.presentation.screens.dashboard.DashboardViewModel
import com.example.presentation.screens.input.InputScreen
import com.example.presentation.screens.input.InputViewModel
import com.example.presentation.screens.onboarding.OnboardingScreen
import com.example.presentation.screens.onboarding.OnboardingViewModel
import com.example.presentation.screens.schedule.ScheduleScreen
import com.example.presentation.screens.schedule.ScheduleViewModel
import com.example.presentation.screens.scenarios.ScenariosScreen
import com.example.presentation.screens.scenarios.ScenariosViewModel
import com.example.presentation.screens.tools.ToolsScreen
import com.example.presentation.screens.tools.ToolsViewModel
import com.example.ui.theme.LoanLabTheme
import com.example.ui.theme.LoanLabColors

// Route definition strings
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_SCHEDULE = "schedule"
const val ROUTE_SCENARIOS = "scenarios"
const val ROUTE_ANNUAL = "annual"
const val ROUTE_TOOLS = "tools"
const val ROUTE_INPUT = "input"
const val ROUTE_ABOUT = "about"

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen(ROUTE_DASHBOARD, "Dashboard", Icons.Default.Home)
    object Schedule : Screen(ROUTE_SCHEDULE, "Schedule", Icons.Default.DateRange)
    object Scenarios : Screen(ROUTE_SCENARIOS, "Scenarios", Icons.Default.Refresh)
    object Annual : Screen(ROUTE_ANNUAL, "Annual", Icons.Default.Info)
    object Tools : Screen(ROUTE_TOOLS, "Tools", Icons.Default.Settings)
    object About : Screen(ROUTE_ABOUT, "About", Icons.Default.AccountBox)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as LoanLabApplication

        setContent {
            LoanLabTheme {
                val onboardingComplete by app.loanRepository.getOnboardingCompletePreference()
                    .collectAsState(initial = null)

                if (onboardingComplete == null) {
                    // Quick placeholder loading
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LoanLabColors.Background)
                    )
                } else if (!onboardingComplete!!) {
                    // First activation wizard
                    val onboardingViewModel: OnboardingViewModel = viewModel(factory = ViewModelFactory(app))
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onFinish = { /* Onboarding complete toggled state will trigger recomposition */ }
                    )
                } else {
                    // Amortization Cockpit Base Shell
                    val navController = rememberNavController()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route

                    val factory = ViewModelFactory(app)

                    // Init viewModels
                    val inputViewModel: InputViewModel = viewModel(factory = factory)
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
                    val scheduleViewModel: ScheduleViewModel = viewModel(factory = factory)
                    val scenariosViewModel: ScenariosViewModel = viewModel(factory = factory)
                    val annualViewModel: AnnualSummaryViewModel = viewModel(factory = factory)
                    val toolsViewModel: ToolsViewModel = viewModel(factory = factory)

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LoanLabColors.Background),
                        containerColor = LoanLabColors.Background,
                        bottomBar = {
                            if (currentRoute != ROUTE_INPUT) {
                                NavigationBar(
                                    containerColor = LoanLabColors.Surface,
                                    tonalElevation = 8.dp
                                ) {
                                    val screens = listOf(
                                        Screen.Dashboard,
                                        Screen.Schedule,
                                        Screen.Scenarios,
                                        Screen.Annual,
                                        Screen.Tools,
                                        Screen.About
                                    )

                                    screens.forEach { screen ->
                                        val active = currentRoute == screen.route
                                        NavigationBarItem(
                                            selected = active,
                                            onClick = {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.title,
                                                    tint = if (active) LoanLabColors.Accent else LoanLabColors.Text3,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = screen.title,
                                                    fontSize = 10.sp,
                                                    color = if (active) LoanLabColors.Text1 else LoanLabColors.Text3
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = LoanLabColors.Accent.copy(alpha = 0.15f)
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        floatingActionButton = {
                            if (currentRoute != ROUTE_INPUT) {
                                FloatingActionButton(
                                    onClick = { navController.navigate(ROUTE_INPUT) },
                                    shape = CircleShape,
                                    containerColor = LoanLabColors.Accent,
                                    contentColor = Color.White,
                                    modifier = Modifier
                                        .testTag("floating_edit_fab")
                                        .size(56.dp)
                                        .clip(CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Config",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_DASHBOARD,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(ROUTE_DASHBOARD) {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onNavigateToInput = { navController.navigate(ROUTE_INPUT) }
                                )
                            }
                            composable(ROUTE_SCHEDULE) {
                                ScheduleScreen(viewModel = scheduleViewModel)
                            }
                            composable(ROUTE_SCENARIOS) {
                                ScenariosScreen(viewModel = scenariosViewModel)
                            }
                            composable(ROUTE_ANNUAL) {
                                AnnualSummaryScreen(viewModel = annualViewModel)
                            }
                            composable(ROUTE_TOOLS) {
                                ToolsScreen(viewModel = toolsViewModel)
                            }
                            composable(ROUTE_ABOUT) {
                                AboutScreen()
                            }
                            composable(ROUTE_INPUT) {
                                InputScreen(
                                    viewModel = inputViewModel,
                                    onCalculate = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
