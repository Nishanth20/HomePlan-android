package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.datastore.UserPreferencesDataStore
import com.example.data.local.db.LoanDatabase
import com.example.data.repository.LoanRepositoryImpl
import com.example.domain.repository.LoanRepository
import com.example.domain.usecase.BuildAnnualSummaryUseCase
import com.example.domain.usecase.PrepaymentImpactUseCase
import com.example.domain.usecase.RateShockUseCase
import com.example.domain.usecase.ScenarioCompareUseCase
import com.example.domain.usecase.SimulateLoanUseCase

class LoanLabApplication : Application() {

    lateinit var loanRepository: LoanRepository
    lateinit var simulateLoanUseCase: SimulateLoanUseCase
    lateinit var scenarioCompareUseCase: ScenarioCompareUseCase
    lateinit var buildAnnualSummaryUseCase: BuildAnnualSummaryUseCase
    lateinit var prepaymentImpactUseCase: PrepaymentImpactUseCase
    lateinit var rateShockUseCase: RateShockUseCase

    override fun onCreate() {
        super.onCreate()

        val database = Room.databaseBuilder(
            applicationContext,
            LoanDatabase::class.java,
            "loan_lab_database"
        ).fallbackToDestructiveMigration().build()

        val preferencesDataStore = UserPreferencesDataStore(applicationContext)

        loanRepository = LoanRepositoryImpl(
            loanProfileDao = database.loanProfileDao(),
            preferencesDataStore = preferencesDataStore
        )

        simulateLoanUseCase = SimulateLoanUseCase()
        scenarioCompareUseCase = ScenarioCompareUseCase(simulateLoanUseCase)
        buildAnnualSummaryUseCase = BuildAnnualSummaryUseCase(simulateLoanUseCase)
        prepaymentImpactUseCase = PrepaymentImpactUseCase(simulateLoanUseCase)
        rateShockUseCase = RateShockUseCase(simulateLoanUseCase)
    }
}
