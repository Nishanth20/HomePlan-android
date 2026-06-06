package com.example.data.repository

import com.example.data.local.datastore.UserPreferencesDataStore
import com.example.data.local.db.LoanProfileDao
import com.example.data.local.db.LoanProfileEntity
import com.example.domain.model.LoanInput
import com.example.domain.model.LoanProfile
import com.example.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoanRepositoryImpl(
    private val loanProfileDao: LoanProfileDao,
    private val preferencesDataStore: UserPreferencesDataStore
) : LoanRepository {

    override fun getSavedProfiles(): Flow<List<LoanProfile>> {
        return loanProfileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveProfile(name: String, input: LoanInput) {
        val entity = LoanProfileEntity.fromDomain(name, input)
        loanProfileDao.insertProfile(entity)
    }

    override suspend fun deleteProfile(id: Int) {
        loanProfileDao.deleteProfileById(id)
    }

    override fun getLastUsedInput(): Flow<LoanInput?> {
        return preferencesDataStore.lastUsedInputFlow
    }

    override suspend fun saveLastUsedInput(input: LoanInput) {
        preferencesDataStore.saveLastUsedInput(input)
    }

    override fun getIndianFormatPreference(): Flow<Boolean> {
        return preferencesDataStore.indianFormatFlow
    }

    override suspend fun saveIndianFormatPreference(enabled: Boolean) {
        preferencesDataStore.saveIndianFormat(enabled)
    }

    override fun getOnboardingCompletePreference(): Flow<Boolean> {
        return preferencesDataStore.onboardingCompleteFlow
    }

    override suspend fun saveOnboardingCompletePreference(enabled: Boolean) {
        preferencesDataStore.saveOnboardingComplete(enabled)
    }
}
