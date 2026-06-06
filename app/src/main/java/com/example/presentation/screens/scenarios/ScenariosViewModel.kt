package com.example.presentation.screens.scenarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LoanInput
import com.example.domain.model.ScenarioResult
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.ScenarioCompareUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScenariosViewModel(
    private val repository: LoanRepository,
    private val scenarioCompareUseCase: ScenarioCompareUseCase
) : ViewModel() {

    private val _scenarios = MutableStateFlow<List<ScenarioResult>>(emptyList())
    val scenarios: StateFlow<List<ScenarioResult>> = _scenarios

    private val _input = MutableStateFlow<LoanInput?>(null)
    val input: StateFlow<LoanInput?> = _input

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
                    runScenarios(lastInput)
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun runScenarios(input: LoanInput) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val results = scenarioCompareUseCase.execute(input)
                _scenarios.value = results
            } catch (e: Exception) {
                _scenarios.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
