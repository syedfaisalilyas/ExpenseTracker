package com.example.expensetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.adapter.RecentExpenseAdapter
import com.example.expensetracker.receiver.DataUpdateReceiver
import com.example.expensetracker.utils.DataManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvMonthlyTotal: TextView
    private lateinit var tvWeeklyTotal: TextView
    private lateinit var tvNoTransactions: TextView
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var recentAdapter: RecentExpenseAdapter

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvMonthlyTotal = findViewById(R.id.tvMonthlyTotal)
        tvWeeklyTotal = findViewById(R.id.tvWeeklyTotal)
        tvNoTransactions = findViewById(R.id.tvNoTransactions)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)

        recentAdapter = RecentExpenseAdapter(emptyList())
        rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        rvRecentTransactions.adapter = recentAdapter

        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        DataManager.loadAll(applicationContext)
        refreshUI()
        val filter = IntentFilter(DataUpdateReceiver.ACTION_DATA_UPDATED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(dataReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        try { unregisterReceiver(dataReceiver) } catch (_: Exception) {}
    }

    override fun onRestart() {
        super.onRestart()
        DataManager.loadAll(applicationContext)
        refreshUI()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        findViewById<View>(R.id.btnViewAll).setOnClickListener {
            startActivity(Intent(this, ViewTransactionsActivity::class.java))
        }
        findViewById<View>(R.id.btnSummary).setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
        findViewById<View>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<View>(R.id.tvViewAllLink).setOnClickListener {
            startActivity(Intent(this, ViewTransactionsActivity::class.java))
        }
    }

    private fun refreshUI() {
        val total = DataManager.getTotalExpenses()
        tvTotalExpenses.text = "PKR ${String.format("%,.0f", total)}"

        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthlyTotal = DataManager.getMonthlyTotal(sdf.format(System.currentTimeMillis()))
        tvMonthlyTotal.text = "PKR ${String.format("%,.0f", monthlyTotal)}"

        val weekDates = getCurrentWeekDates()
        val weeklyTotal = DataManager.getWeeklyTotal(weekDates)
        tvWeeklyTotal.text = "PKR ${String.format("%,.0f", weeklyTotal)}"

        val profile = DataManager.getProfile()
        val name = profile.name.ifEmpty { "User" }
        supportActionBar?.subtitle = "Welcome, $name!"

        val recent = DataManager.getRecentExpenses(5)
        if (recent.isEmpty()) {
            tvNoTransactions.visibility = View.VISIBLE
            rvRecentTransactions.visibility = View.GONE
        } else {
            tvNoTransactions.visibility = View.GONE
            rvRecentTransactions.visibility = View.VISIBLE
            recentAdapter.updateData(recent)
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
}
