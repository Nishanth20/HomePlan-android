package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.LoanLabApplication
import com.example.presentation.screens.annual.AnnualSummaryViewModel
import com.example.presentation.screens.dashboard.DashboardViewModel
import com.example.presentation.screens.input.InputViewModel
import com.example.presentation.screens.schedule.ScheduleViewModel
import com.example.presentation.screens.scenarios.ScenariosViewModel
import com.example.presentation.screens.tools.ToolsViewModel
import com.example.presentation.screens.onboarding.OnboardingViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val application: LoanLabApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = application.loanRepository
        val sim = application.simulateLoanUseCase
        val comp = application.scenarioCompareUseCase
        val ann = application.buildAnnualSummaryUseCase
        val imp = application.prepaymentImpactUseCase
        val shock = application.rateShockUseCase

        return when {
            modelClass.isAssignableFrom(InputViewModel::class.java) -> InputViewModel(repo, sim) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repo, sim, comp, ann) as T
            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> ScheduleViewModel(repo, sim) as T
            modelClass.isAssignableFrom(ScenariosViewModel::class.java) -> ScenariosViewModel(repo, comp) as T
            modelClass.isAssignableFrom(AnnualSummaryViewModel::class.java) -> AnnualSummaryViewModel(repo, ann) as T
            modelClass.isAssignableFrom(ToolsViewModel::class.java) -> ToolsViewModel(repo, imp, shock) as T
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> OnboardingViewModel(repo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
