package com.example.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LoanInput
import com.example.domain.model.MonthRow
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.SimulateLoanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScheduleFilter {
    ALL, PREPAYMENTS_ONLY, YEARS_1_5, YEARS_6_10, YEARS_11_PLUS
}

class ScheduleViewModel(
    private val repository: LoanRepository,
    private val simulateUseCase: SimulateLoanUseCase
) : ViewModel() {

    private val _input = MutableStateFlow<LoanInput?>(null)
    val input: StateFlow<LoanInput?> = _input

    private val _monthRows = MutableStateFlow<List<MonthRow>>(emptyList())
    val monthRows: StateFlow<List<MonthRow>> = _monthRows

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentFilter = MutableStateFlow(ScheduleFilter.ALL)
    val currentFilter: StateFlow<ScheduleFilter> = _currentFilter

    // Filtered list using StateFlow combination
    val filteredRows: StateFlow<List<MonthRow>> = combine(
        _monthRows, _searchQuery, _currentFilter
    ) { rows, query, filter ->
        var res = rows

        // Apply filters
        res = when (filter) {
            ScheduleFilter.ALL -> res
            ScheduleFilter.PREPAYMENTS_ONLY -> res.filter { it.prepaymentApplied > 0.05 }
            ScheduleFilter.YEARS_1_5 -> res.filter { it.monthNo in 1..60 }
            ScheduleFilter.YEARS_6_10 -> res.filter { it.monthNo in 61..120 }
            ScheduleFilter.YEARS_11_PLUS -> res.filter { it.monthNo > 120 }
        }

        // Apply query
        if (query.isNotBlank()) {
            val qInt = query.toIntOrNull()
            res = if (qInt != null) {
                res.filter { it.monthNo == qInt }
            } else {
                res.filter { it.monthLabel.contains(query, ignoreCase = true) }
            }
        }
        res
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            repository.getLastUsedInput().collectLatest { lastInput ->
                if (lastInput != null) {
                    _input.value = lastInput
                    simulate(lastInput)
                }
            }
        }
    }

    private fun simulate(loanInput: LoanInput) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val (rows, _) = simulateUseCase.execute(loanInput)
                _monthRows.value = rows
            } catch (e: Exception) {
                _monthRows.value = emptyList()
            }
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateFilter(filter: ScheduleFilter) { _currentFilter.value = filter }
}
