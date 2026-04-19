package com.example.expensetracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.expensetracker.utils.DataManager
import com.example.expensetracker.view.BarChartView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SummaryActivity : AppCompatActivity() {

    private lateinit var tvWeeklyTotal: TextView
    private lateinit var tvWeeklyTarget: TextView
    private lateinit var tvStatus: TextView
    private lateinit var etWeeklyTarget: TextInputEditText
    private lateinit var barChart: BarChartView
    private lateinit var llCategoryBreakdown: LinearLayout
    private lateinit var tvNoCategoryData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvWeeklyTotal = findViewById(R.id.tvWeeklyTotal)
        tvWeeklyTarget = findViewById(R.id.tvWeeklyTarget)
        tvStatus = findViewById(R.id.tvStatus)
        etWeeklyTarget = findViewById(R.id.etWeeklyTarget)
        barChart = findViewById(R.id.barChart)
        llCategoryBreakdown = findViewById(R.id.llCategoryBreakdown)
        tvNoCategoryData = findViewById(R.id.tvNoCategoryData)

        val currentTarget = DataManager.getProfile().weeklyTarget
        if (currentTarget > 0) {
            etWeeklyTarget.setText(currentTarget.toLong().toString())
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSetTarget)
            .setOnClickListener { onSetTargetClicked() }
    }

    override fun onStart() {
        super.onStart()
        DataManager.loadAll(applicationContext)
        refreshSummary()
    }

    override fun onResume() {
        super.onResume()
        refreshSummary()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        refreshSummary()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun onSetTargetClicked() {
        val targetStr = etWeeklyTarget.text.toString().trim()
        if (targetStr.isEmpty()) {
            Toast.makeText(this, "Please enter a target amount", Toast.LENGTH_SHORT).show()
            return
        }
        val target = targetStr.toDoubleOrNull()
        if (target == null || target <= 0) {
            Toast.makeText(this, "Enter a valid positive amount", Toast.LENGTH_SHORT).show()
            return
        }
        DataManager.saveWeeklyTarget(applicationContext, target)
        Toast.makeText(this, "Weekly target set to PKR ${String.format("%,.0f", target)}", Toast.LENGTH_SHORT).show()
        refreshSummary()
    }

    private fun refreshSummary() {
        val weekDates = getCurrentWeekDates()
        val dayLabels = getWeekDayLabels()
        val weeklyTotal = DataManager.getWeeklyTotal(weekDates)
        val weeklyTarget = DataManager.getProfile().weeklyTarget

        tvWeeklyTotal.text = "PKR ${String.format("%,.0f", weeklyTotal)}"
        tvWeeklyTarget.text = if (weeklyTarget > 0) "PKR ${String.format("%,.0f", weeklyTarget)}" else "Not set"

        if (weeklyTarget > 0) {
            if (weeklyTotal > weeklyTarget) {
                tvStatus.text = "Over Budget ⚠️"
                tvStatus.setTextColor(Color.parseColor("#C0392B"))
            } else {
                val pct = (weeklyTotal / weeklyTarget * 100).toInt()
                tvStatus.text = "On Track ✓ ($pct%)"
                tvStatus.setTextColor(Color.parseColor("#DBC2A6"))
            }
        } else {
            tvStatus.text = "No Target Set"
            tvStatus.setTextColor(Color.parseColor("#B0C4A8"))
        }

        // Build daily totals for chart
        val dailyValues = FloatArray(7)
        for (i in 0 until 7) {
            dailyValues[i] = DataManager.getDailyTotal(weekDates[i]).toFloat()
        }
        barChart.setData(dailyValues, dayLabels.toTypedArray(), weeklyTarget.toFloat())

        // Category breakdown
        buildCategoryBreakdown(weekDates)
    }

    private fun buildCategoryBreakdown(weekDates: List<String>) {
        val expenses = DataManager.getAllExpenses().filter { it.date in weekDates }
        llCategoryBreakdown.removeAllViews()

        if (expenses.isEmpty()) {
            tvNoCategoryData.visibility = View.VISIBLE
            return
        }
        tvNoCategoryData.visibility = View.GONE

        val categoryMap = expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val total = expenses.sumOf { it.amount }

        for ((category, amount) in categoryMap) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 6, 0, 6)
            }

            val emoji = getCategoryEmoji(category)

            val tvCat = TextView(this).apply {
                text = "$emoji $category"
                textSize = 14f
                setTextColor(Color.parseColor("#1A1A1A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val pct = if (total > 0) (amount / total * 100).toInt() else 0
            val tvAmt = TextView(this).apply {
                text = "PKR ${String.format("%,.0f", amount)} ($pct%)"
                textSize = 14f
                setTextColor(Color.parseColor("#99744A"))
            }

            row.addView(tvCat)
            row.addView(tvAmt)
            llCategoryBreakdown.addView(row)

            // Progress bar for category
            val progressBg = View(this).apply {
                setBackgroundColor(Color.parseColor("#F0E8DC"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 8
                ).also { it.topMargin = 2; it.bottomMargin = 4 }
            }
            val progressContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 8
                ).also { it.topMargin = 2; it.bottomMargin = 6 }
                setBackgroundColor(Color.parseColor("#F0E8DC"))
            }
            val progressFill = View(this).apply {
                setBackgroundColor(Color.parseColor("#99744A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, pct.toFloat())
            }
            val progressEmpty = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (100 - pct).toFloat())
            }
            progressContainer.addView(progressFill)
            progressContainer.addView(progressEmpty)
            llCategoryBreakdown.addView(progressContainer)
        }
    }

    private fun getCurrentWeekDates(): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return (0..6).map {
            val date = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            date
        }
    }

    private fun getWeekDayLabels(): List<String> {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return (0..6).map {
            val label = sdf.format(cal.time).substring(0, 3)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            label
        }
    }

    private fun getCategoryEmoji(category: String): String = when (category.lowercase()) {
        "food" -> "🍔"
        "transport" -> "🚗"
        "shopping" -> "🛍️"
        "health" -> "💊"
        "education" -> "📚"
        "entertainment" -> "🎬"
        "utilities" -> "💡"
        "rent" -> "🏠"
        "clothing" -> "👕"
        else -> "💰"
    }
}
