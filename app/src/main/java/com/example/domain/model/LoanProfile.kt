package com.example.domain.model

data class LoanProfile(
    val id: Int,
    val name: String,
    val input: LoanInput,
    val createdAt: Long
)
