package com.example.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.db.LoanProfileEntity
import com.example.domain.model.LoanInput
import com.example.domain.model.PrepaymentMode
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        private val KEY_PRINCIPAL = doublePreferencesKey("principal")
        private val KEY_ANNUAL_RATE = doublePreferencesKey("annual_rate")
        private val KEY_TENURE_YEARS = doublePreferencesKey("tenure_years")
        private val KEY_MODE = stringPreferencesKey("mode")
        private val KEY_START_DATE = stringPreferencesKey("start_date")
        private val KEY_LUMP_SUM = stringPreferencesKey("lump_sum")
        private val KEY_EXTRA_EMI = stringPreferencesKey("extra_emi")
        private val KEY_RATE_CHANGES = stringPreferencesKey("rate_changes")
        private val KEY_INDIAN_FORMAT = booleanPreferencesKey("indian_format")
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val lastUsedInputFlow: Flow<LoanInput?> = context.dataStore.data.map { preferences ->
        val principal = preferences[KEY_PRINCIPAL] ?: return@map null
        val rate = preferences[KEY_ANNUAL_RATE] ?: 9.0
        val tenure = preferences[KEY_TENURE_YEARS] ?: 20.0
        val modeStr = preferences[KEY_MODE] ?: PrepaymentMode.REDUCE_TENURE.name
        val startDateStr = preferences[KEY_START_DATE] ?: LocalDate.now().toString()
        val lumpSumStr = preferences[KEY_LUMP_SUM] ?: ""
        val extraEmiStr = preferences[KEY_EXTRA_EMI] ?: ""
        val rateChangesStr = preferences[KEY_RATE_CHANGES] ?: ""

        val mode = try { PrepaymentMode.valueOf(modeStr) } catch (e: Exception) { PrepaymentMode.REDUCE_TENURE }
        val startDate = try { LocalDate.parse(startDateStr) } catch (e: Exception) { LocalDate.now() }

        LoanInput(
            principal = principal,
            annualRate = rate,
            tenureYears = tenure,
            mode = mode,
            startDate = startDate,
            lumpSumEntries = LoanProfileEntity.deserializeMapDouble(lumpSumStr),
            extraEmiEntries = LoanProfileEntity.deserializeMapInt(extraEmiStr),
            rateChanges = LoanProfileEntity.deserializeMapDouble(rateChangesStr)
        )
    }

    suspend fun saveLastUsedInput(input: LoanInput) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PRINCIPAL] = input.principal
            preferences[KEY_ANNUAL_RATE] = input.annualRate
            preferences[KEY_TENURE_YEARS] = input.tenureYears
            preferences[KEY_MODE] = input.mode.name
            preferences[KEY_START_DATE] = input.startDate.toString()
            preferences[KEY_LUMP_SUM] = LoanProfileEntity.serializeMap(input.lumpSumEntries)
            preferences[KEY_EXTRA_EMI] = LoanProfileEntity.serializeMap(input.extraEmiEntries)
            preferences[KEY_RATE_CHANGES] = LoanProfileEntity.serializeMap(input.rateChanges)
        }
    }

    val indianFormatFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_INDIAN_FORMAT] ?: true
    }

    suspend fun saveIndianFormat(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INDIAN_FORMAT] = enabled
        }
    }

    val onboardingCompleteFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETE] ?: false
    }

    suspend fun saveOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETE] = complete
        }
    }
}
