package com.example.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.LoanRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: LoanRepository) : ViewModel() {

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveOnboardingCompletePreference(true)
            onSuccess()
        }
    }
}
