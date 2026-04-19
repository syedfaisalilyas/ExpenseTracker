package com.example.expensetracker.utils

import android.content.Context
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object DataManager {

    private const val EXPENSES_FILE = "expenses.json"
    private const val PROFILE_FILE = "profile.json"

    private val expenses = mutableListOf<Expense>()
    private var profile = UserProfile()
    private var nextId = 1

    fun loadAll(context: Context) {
        loadExpenses(context)
        loadProfile(context)
    }

    // ── Expenses ──────────────────────────────────────────────────────────────

    fun loadExpenses(context: Context) {
        expenses.clear()
        val file = File(context.filesDir, EXPENSES_FILE)
        if (!file.exists()) return
        try {
            val json = file.readText()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                expenses.add(
                    Expense(
                        id = obj.getInt("id"),
                        amount = obj.getDouble("amount"),
                        category = obj.getString("category"),
                        note = obj.optString("note", ""),
                        date = obj.optString("date", "")
                    )
                )
            }
            nextId = if (expenses.isEmpty()) 1 else expenses.maxOf { it.id } + 1
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveExpenses(context: Context) {
        val arr = JSONArray()
        for (e in expenses) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("amount", e.amount)
            obj.put("category", e.category)
            obj.put("note", e.note)
            obj.put("date", e.date)
            arr.put(obj)
        }
        File(context.filesDir, EXPENSES_FILE).writeText(arr.toString())
    }

    fun addExpense(context: Context, expense: Expense): Expense {
        val newExpense = expense.copy(id = nextId++)
        expenses.add(newExpense)
        saveExpenses(context)
        return newExpense
    }

    fun updateExpense(context: Context, updated: Expense) {
        val idx = expenses.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            expenses[idx] = updated
            saveExpenses(context)
        }
    }

    fun deleteExpense(context: Context, id: Int) {
        expenses.removeAll { it.id == id }
        saveExpenses(context)
    }

    fun getAllExpenses(): List<Expense> = expenses.toList().sortedByDescending { it.date }

    fun getRecentExpenses(limit: Int = 5): List<Expense> = getAllExpenses().take(limit)

    fun getTotalExpenses(): Double = expenses.sumOf { it.amount }

    fun getMonthlyTotal(yearMonth: String): Double =
        expenses.filter { it.date.startsWith(yearMonth) }.sumOf { it.amount }

    fun getWeeklyTotal(dates: List<String>): Double =
        expenses.filter { it.date in dates }.sumOf { it.amount }

    fun getDailyTotal(date: String): Double =
        expenses.filter { it.date == date }.sumOf { it.amount }

    fun resetAllData(context: Context) {
        expenses.clear()
        nextId = 1
        saveExpenses(context)
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    fun loadProfile(context: Context) {
        val file = File(context.filesDir, PROFILE_FILE)
        if (!file.exists()) return
        try {
            val obj = JSONObject(file.readText())
            profile = UserProfile(
                name = obj.optString("name", ""),
                monthlyBudget = obj.optDouble("monthlyBudget", 0.0),
                weeklyTarget = obj.optDouble("weeklyTarget", 0.0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveProfile(context: Context, p: UserProfile) {
        profile = p
        val obj = JSONObject()
        obj.put("name", p.name)
        obj.put("monthlyBudget", p.monthlyBudget)
        obj.put("weeklyTarget", p.weeklyTarget)
        File(context.filesDir, PROFILE_FILE).writeText(obj.toString())
    }

    fun getProfile(): UserProfile = profile

    fun saveWeeklyTarget(context: Context, target: Double) {
        saveProfile(context, profile.copy(weeklyTarget = target))
    }
}
