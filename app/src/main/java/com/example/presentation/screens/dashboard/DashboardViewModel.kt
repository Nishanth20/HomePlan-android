package com.example.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LoanInput
import com.example.domain.model.LoanSummary
import com.example.domain.model.MonthRow
import com.example.domain.model.PrepaymentMode
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.BuildAnnualSummaryUseCase
import com.example.domain.usecase.ScenarioCompareUseCase
import com.example.domain.usecase.SimulateLoanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: LoanRepository,
    private val simulateUseCase: SimulateLoanUseCase,
    private val scenarioCompareUseCase: ScenarioCompareUseCase,
    private val buildAnnualSummaryUseCase: BuildAnnualSummaryUseCase
) : ViewModel() {

    private val _input = MutableStateFlow<LoanInput?>(null)
    val input: StateFlow<LoanInput?> = _input

    private val _monthRows = MutableStateFlow<List<MonthRow>>(emptyList())
    val monthRows: StateFlow<List<MonthRow>> = _monthRows

    private val _summary = MutableStateFlow<LoanSummary?>(null)
    val summary: StateFlow<LoanSummary?> = _summary

    private val _baseline = MutableStateFlow<LoanSummary?>(null)
    val baseline: StateFlow<LoanSummary?> = _baseline

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
                    simulate(lastInput)
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun simulate(loanInput: LoanInput) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Calculate baseline summary (no prepayments)
                val baseInput = loanInput.copy(
                    mode = PrepaymentMode.REDUCE_TENURE,
                    lumpSumEntries = emptyMap(),
                    extraEmiEntries = emptyMap()
                )
                val (_, baseSummary) = simulateUseCase.execute(baseInput)
                _baseline.value = baseSummary

                // Calculate actual simulated schedule and summary
                val (rows, actualSummary) = simulateUseCase.execute(loanInput)
                _monthRows.value = rows
                _summary.value = actualSummary
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Simulation error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
