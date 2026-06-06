package com.example.presentation.screens.tools

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LoanInput
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.PrepaymentImpactUseCase
import com.example.domain.usecase.RateShockUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ToolsViewModel(
    private val repository: LoanRepository,
    private val prepayUseCase: PrepaymentImpactUseCase,
    private val rateShockUseCase: RateShockUseCase
) : ViewModel() {

    private val _input = MutableStateFlow<LoanInput?>(null)
    val input: StateFlow<LoanInput?> = _input

    // Reactive parameters for calculators
    private val _prepayAmount = MutableStateFlow(200000.0) // Double 2 Lakhs
    val prepayAmount: StateFlow<Double> = _prepayAmount

    private val _prepayMonth = MutableStateFlow(12) // Int Month 12
    val prepayMonth: StateFlow<Int> = _prepayMonth

    private val _rateShockDelta = MutableStateFlow(1.5) // Double +1.5%
    val rateShockDelta: StateFlow<Double> = _rateShockDelta

    // Instantly readable UI state properties
    var prepayInterestSaved by mutableStateOf(0.0)
    var prepayMonthsSaved by mutableStateOf(0)

    var newEmi by mutableStateOf(0.0)
    var emiDelta by mutableStateOf(0.0)
    var totalInterestDelta by mutableStateOf(0.0)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            repository.getLastUsedInput().collectLatest { lastInput ->
                if (lastInput != null) {
                    _input.value = lastInput
                    recalculateImpact(lastInput, _prepayAmount.value, _prepayMonth.value)
                    recalculateShock(lastInput, _rateShockDelta.value)
                }
            }
        }
    }

    fun updatePrepayAmount(amount: Double) {
        _prepayAmount.value = amount
        _input.value?.let { recalculateImpact(it, amount, _prepayMonth.value) }
    }

    fun updatePrepayMonth(month: Int) {
        _prepayMonth.value = month
        _input.value?.let { recalculateImpact(it, _prepayAmount.value, month) }
    }

    fun updateRateShock(delta: Double) {
        _rateShockDelta.value = delta
        _input.value?.let { recalculateShock(it, delta) }
    }

    private fun recalculateImpact(input: LoanInput, amt: Double, month: Int) {
        // Execute expects: execute(currentInput, monthN, amountX)
        val res = prepayUseCase.execute(input, month, amt)
        prepayInterestSaved = res.interestSaved
        prepayMonthsSaved = res.monthsSaved
    }

    private fun recalculateShock(input: LoanInput, delta: Double) {
        // Execute expects: execute(currentInput, monthN, newRateX)
        val res = rateShockUseCase.execute(input, 1, input.annualRate + delta)
        newEmi = res.newEmiAfterMonthN
        
        // Compute base EMI manually to estimate delta
        val r = (input.annualRate / 1200)
        val n = input.tenureYears * 12
        val originalEmiCalculated = if (r > 0) {
            input.principal * r * java.lang.Math.pow(1 + r, n.toDouble()) / (java.lang.Math.pow(1 + r, n.toDouble()) - 1)
        } else {
            input.principal / n.toDouble()
        }

        emiDelta = res.newEmiAfterMonthN - originalEmiCalculated
        totalInterestDelta = res.extraInterestCost
    }
}
