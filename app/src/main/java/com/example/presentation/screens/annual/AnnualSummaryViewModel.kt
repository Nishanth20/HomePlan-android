package com.example.presentation.screens.annual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AnnualSummaryRow
import com.example.domain.model.LoanInput
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.BuildAnnualSummaryUseCase
import com.example.domain.usecase.SimulateLoanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AnnualSummaryViewModel(
    private val repository: LoanRepository,
    private val buildAnnualSummaryUseCase: BuildAnnualSummaryUseCase
) : ViewModel() {

    private val _input = MutableStateFlow<LoanInput?>(null)
    val input: StateFlow<LoanInput?> = _input

    private val _annualRows = MutableStateFlow<List<AnnualSummaryRow>>(emptyList())
    val annualRows: StateFlow<List<AnnualSummaryRow>> = _annualRows

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getLastUsedInput().collectLatest { lastInput ->
                if (lastInput != null) {
                    _input.value = lastInput
                    calculateAnnualRows(lastInput)
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun calculateAnnualRows(input: LoanInput) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val simulateUseCase = SimulateLoanUseCase()
                val (months, _) = simulateUseCase.execute(input)
                val years = buildAnnualSummaryUseCase.execute(months)
                _annualRows.value = years
            } catch (e: Exception) {
                _annualRows.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
