package com.example.domain.repository

import com.example.domain.model.LoanInput
import com.example.domain.model.LoanProfile
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun getSavedProfiles(): Flow<List<LoanProfile>>
    suspend fun saveProfile(name: String, input: LoanInput)
    suspend fun deleteProfile(id: Int)
    
    // Preferences DataStore
    fun getLastUsedInput(): Flow<LoanInput?>
    suspend fun saveLastUsedInput(input: LoanInput)
    fun getIndianFormatPreference(): Flow<Boolean>
    suspend fun saveIndianFormatPreference(enabled: Boolean)
    fun getOnboardingCompletePreference(): Flow<Boolean>
    suspend fun saveOnboardingCompletePreference(enabled: Boolean)
}
