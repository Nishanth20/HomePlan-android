package com.example.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.LoanInput
import com.example.domain.model.LoanProfile
import com.example.domain.model.PrepaymentMode
import java.time.LocalDate

@Entity(tableName = "loan_profiles")
data class LoanProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val principal: Double,
    @ColumnInfo(name = "annual_rate") val annualRate: Double,
    @ColumnInfo(name = "tenure_years") val tenureYears: Double,
    val mode: String,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "lump_sum_json") val lumpSumJson: String,
    @ColumnInfo(name = "extra_emi_json") val extraEmiJson: String,
    @ColumnInfo(name = "rate_changes_json") val rateChangesJson: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): LoanProfile {
        return LoanProfile(
            id = id,
            name = name,
            createdAt = createdAt,
            input = LoanInput(
                principal = principal,
                annualRate = annualRate,
                tenureYears = tenureYears,
                mode = PrepaymentMode.valueOf(mode),
                startDate = LocalDate.parse(startDate),
                lumpSumEntries = deserializeMapDouble(lumpSumJson),
                extraEmiEntries = deserializeMapInt(extraEmiJson),
                rateChanges = deserializeMapDouble(rateChangesJson)
            )
        )
    }

    companion object {
        fun fromDomain(name: String, input: LoanInput): LoanProfileEntity {
            return LoanProfileEntity(
                name = name,
                principal = input.principal,
                annualRate = input.annualRate,
                tenureYears = input.tenureYears,
                mode = input.mode.name,
                startDate = input.startDate.toString(),
                lumpSumJson = serializeMap(input.lumpSumEntries),
                extraEmiJson = serializeMap(input.extraEmiEntries),
                rateChangesJson = serializeMap(input.rateChanges)
            )
        }

        fun serializeMap(map: Map<Int, out Number>): String {
            if (map.isEmpty()) return ""
            return map.entries.joinToString(",") { "${it.key}:${it.value}" }
        }

        fun deserializeMapDouble(str: String?): Map<Int, Double> {
            if (str.isNullOrBlank()) return emptyMap()
            return try {
                str.split(",").associate {
                    val parts = it.split(":")
                    parts[0].toInt() to parts[1].toDouble()
                }
            } catch (e: Exception) {
                emptyMap()
            }
        }

        fun deserializeMapInt(str: String?): Map<Int, Int> {
            if (str.isNullOrBlank()) return emptyMap()
            return try {
                str.split(",").associate {
                    val parts = it.split(":")
                    parts[0].toInt() to parts[1].toInt()
                }
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
}
