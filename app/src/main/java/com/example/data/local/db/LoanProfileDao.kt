package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanProfileDao {

    @Query("SELECT * FROM loan_profiles ORDER BY created_at DESC")
    fun getAllProfiles(): Flow<List<LoanProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: LoanProfileEntity)

    @Query("DELETE FROM loan_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)
}
