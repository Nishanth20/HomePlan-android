package com.example.presentation.screens.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LoanInput
import com.example.domain.model.LoanProfile
import com.example.domain.model.PrepaymentMode
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.SimulateLoanUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class InputViewModel(
    private val repository: LoanRepository,
    private val simulateUseCase: SimulateLoanUseCase
) : ViewModel() {

    // Form inputs state Flow
    private val _principal = MutableStateFlow("4500000")
    val principal: StateFlow<String> = _principal

    private val _rate = MutableStateFlow("9.0")
    val rate: StateFlow<String> = _rate

    private val _tenure = MutableStateFlow("20")
    val tenure: StateFlow<String> = _tenure

    private val _mode = MutableStateFlow(PrepaymentMode.REDUCE_TENURE)
    val mode: StateFlow<PrepaymentMode> = _mode

    private val _startDate = MutableStateFlow(LocalDate.now())
    val startDate: StateFlow<LocalDate> = _startDate

    // Dynamic Lists (Month No -> Value)
    private val _lumpSums = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val lumpSums: StateFlow<Map<Int, Double>> = _lumpSums

    private val _extraEmis = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val extraEmis: StateFlow<Map<Int, Int>> = _extraEmis

    private val _rateChanges = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val rateChanges: StateFlow<Map<Int, Double>> = _rateChanges

    // Saved Profiles
    val savedProfiles: StateFlow<List<LoanProfile>> = repository.getSavedProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Validation State
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage

    init {
        // Load initial state (last used inputs)
        viewModelScope.launch {
            repository.getLastUsedInput().firstOrNull()?.let { last ->
                _principal.value = last.principal.toLong().toString()
                _rate.value = last.annualRate.toString()
                _tenure.value = last.tenureYears.toInt().toString()
                _mode.value = last.mode
                _startDate.value = last.startDate
                _lumpSums.value = last.lumpSumEntries
                _extraEmis.value = last.extraEmiEntries
                _rateChanges.value = last.rateChanges
            }
        }
    }

    fun updatePrincipal(value: String) { _principal.value = value }
    fun updateRate(value: String) { _rate.value = value }
    fun updateTenure(value: String) { _tenure.value = value }
    fun updateMode(value: PrepaymentMode) { _mode.value = value }
    fun updateStartDate(value: LocalDate) { _startDate.value = value }

    fun addLumpSum(month: Int, amount: Double) {
        val updated = _lumpSums.value.toMutableMap()
        updated[month] = amount
        _lumpSums.value = updated
    }
    fun removeLumpSum(month: Int) {
        val updated = _lumpSums.value.toMutableMap()
        updated.remove(month)
        _lumpSums.value = updated
    }

    fun addExtraEmi(month: Int, count: Int) {
        val updated = _extraEmis.value.toMutableMap()
        updated[month] = count
        _extraEmis.value = updated
    }
    fun removeExtraEmi(month: Int) {
        val updated = _extraEmis.value.toMutableMap()
        updated.remove(month)
        _extraEmis.value = updated
    }

    fun addRateChange(month: Int, newRate: Double) {
        val updated = _rateChanges.value.toMutableMap()
        updated[month] = newRate
        _rateChanges.value = updated
    }
    fun removeRateChange(month: Int) {
        val updated = _rateChanges.value.toMutableMap()
        updated.remove(month)
        _rateChanges.value = updated
    }

    fun buildInput(): LoanInput? {
        val amount = _principal.value.toDoubleOrNull() ?: 0.0
        val r = _rate.value.toDoubleOrNull() ?: 0.0
        val t = _tenure.value.toDoubleOrNull() ?: 0.0

        if (amount <= 0) {
            viewModelScope.launch { _errorMessage.emit("Principal Amount must be greater than 0") }
            return null
        }
        if (r < 0 || r > 30) {
            viewModelScope.launch { _errorMessage.emit("Rate must be between 0% and 30%") }
            return null
        }
        if (t <= 0 || t > 40) {
            viewModelScope.launch { _errorMessage.emit("Tenure must be between 1 and 40 years") }
            return null
        }

        return LoanInput(
            principal = amount,
            annualRate = r,
            tenureYears = t,
            mode = _mode.value,
            startDate = _startDate.value,
            lumpSumEntries = _lumpSums.value,
            extraEmiEntries = _extraEmis.value,
            rateChanges = _rateChanges.value
        )
    }

    fun saveLastUsed() {
        val input = buildInput() ?: return
        viewModelScope.launch {
            repository.saveLastUsedInput(input)
        }
    }

    fun saveProfile(profileName: String) {
        val input = buildInput() ?: return
        viewModelScope.launch {
            repository.saveProfile(profileName, input)
        }
    }

    fun loadProfile(profile: LoanProfile) {
        _principal.value = profile.input.principal.toLong().toString()
        _rate.value = profile.input.annualRate.toString()
        _tenure.value = profile.input.tenureYears.toInt().toString()
        _mode.value = profile.input.mode
        _startDate.value = profile.input.startDate
        _lumpSums.value = profile.input.lumpSumEntries
        _extraEmis.value = profile.input.extraEmiEntries
        _rateChanges.value = profile.input.rateChanges
    }

    fun deleteProfile(id: Int) {
        viewModelScope.launch {
            repository.deleteProfile(id)
        }
    }
}
