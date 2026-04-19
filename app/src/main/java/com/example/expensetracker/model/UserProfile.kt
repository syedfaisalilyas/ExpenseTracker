package com.example.expensetracker.model

data class UserProfile(
    val name: String = "",
    val monthlyBudget: Double = 0.0,
    val weeklyTarget: Double = 0.0
)
