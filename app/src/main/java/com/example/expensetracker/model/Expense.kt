package com.example.expensetracker.model

data class Expense(
    val id: Int,
    val amount: Double,
    val category: String,
    val note: String,
    val date: String  // format: yyyy-MM-dd
)
