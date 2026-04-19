package com.example.expensetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.adapter.ExpenseAdapter
import com.example.expensetracker.model.Expense
import com.example.expensetracker.receiver.DataUpdateReceiver
import com.example.expensetracker.utils.DataManager

class ViewTransactionsActivity : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotalSummary: TextView
    private lateinit var tvCountSummary: TextView
    private lateinit var expenseAdapter: ExpenseAdapter

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadAndRefresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transactions)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvEmpty = findViewById(R.id.tvEmpty)
        tvTotalSummary = findViewById(R.id.tvTotalSummary)
        tvCountSummary = findViewById(R.id.tvCountSummary)
        rvTransactions = findViewById(R.id.rvTransactions)

        expenseAdapter = ExpenseAdapter(
            mutableListOf(),
            onEdit = { expense -> openEditScreen(expense) },
            onDelete = { expense -> confirmDelete(expense) }
        )
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = expenseAdapter
    }

    override fun onStart() {
        super.onStart()
        DataManager.loadExpenses(applicationContext)
        loadAndRefresh()
        val filter = IntentFilter(DataUpdateReceiver.ACTION_DATA_UPDATED)
        registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onResume() {
        super.onResume()
        loadAndRefresh()
    }

    override fun onStop() {
        super.onStop()
        try { unregisterReceiver(dataReceiver) } catch (_: Exception) {}
    }

    override fun onRestart() {
        super.onRestart()
        DataManager.loadExpenses(applicationContext)
        loadAndRefresh()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadAndRefresh() {
        val allExpenses = DataManager.getAllExpenses()
        if (allExpenses.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvTransactions.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvTransactions.visibility = View.VISIBLE
            expenseAdapter.updateData(allExpenses)
        }
        val total = DataManager.getTotalExpenses()
        tvTotalSummary.text = "PKR ${String.format("%,.0f", total)}"
        tvCountSummary.text = "${allExpenses.size} items"
    }

    private fun openEditScreen(expense: Expense) {
        val intent = Intent(this, AddExpenseActivity::class.java).apply {
            putExtra("expense_id", expense.id)
            putExtra("expense_amount", expense.amount)
            putExtra("expense_category", expense.category)
            putExtra("expense_note", expense.note)
        }
        startActivity(intent)
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage(getString(R.string.delete_confirm))
            .setPositiveButton("Delete") { _, _ ->
                DataManager.deleteExpense(applicationContext, expense.id)
                sendBroadcast(Intent(DataUpdateReceiver.ACTION_DATA_UPDATED))
                loadAndRefresh()
                Toast.makeText(this, getString(R.string.expense_deleted), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
