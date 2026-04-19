package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.expensetracker.model.Expense
import com.example.expensetracker.receiver.DataUpdateReceiver
import com.example.expensetracker.utils.DataManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var tilAmount: TextInputLayout
    private lateinit var etAmount: TextInputEditText
    private lateinit var tilCategory: TextInputLayout
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var tilNote: TextInputLayout
    private lateinit var etNote: TextInputEditText

    private var editExpenseId: Int = -1

    private val categories = listOf(
        "Food", "Transport", "Shopping", "Health",
        "Education", "Entertainment", "Utilities", "Rent", "Clothing", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tilAmount = findViewById(R.id.tilAmount)
        etAmount = findViewById(R.id.etAmount)
        tilCategory = findViewById(R.id.tilCategory)
        actvCategory = findViewById(R.id.actvCategory)
        tilNote = findViewById(R.id.tilNote)
        etNote = findViewById(R.id.etNote)

        setupCategoryDropdown()
        setupFocusListeners()
        checkEditMode()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave)
            .setOnClickListener { onSaveClicked() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun checkEditMode() {
        editExpenseId = intent.getIntExtra("expense_id", -1)
        if (editExpenseId != -1) {
            supportActionBar?.title = "Edit Expense"
            etAmount.setText(intent.getDoubleExtra("expense_amount", 0.0).toLong().toString())
            actvCategory.setText(intent.getStringExtra("expense_category") ?: "", false)
            etNote.setText(intent.getStringExtra("expense_note") ?: "")
        } else {
            supportActionBar?.title = "Add Expense"
        }
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(adapter)
    }

    private fun setupFocusListeners() {
        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = etAmount.text.toString().trim()
                if (text.isEmpty()) {
                    tilAmount.error = "Amount is required"
                } else if (text.toDoubleOrNull() == null || text.toDouble() <= 0) {
                    tilAmount.error = "Enter a valid amount"
                } else {
                    tilAmount.error = null
                }
            }
        }

        actvCategory.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && actvCategory.text.isNullOrEmpty()) {
                tilCategory.error = "Category is required"
            } else {
                tilCategory.error = null
            }
        }
    }

    private fun validateInputs(): Boolean {
        var valid = true

        val amountStr = etAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            tilAmount.error = "Amount is required"
            valid = false
        } else if (amountStr.toDoubleOrNull() == null || amountStr.toDouble() <= 0) {
            tilAmount.error = "Enter a valid positive amount"
            valid = false
        } else {
            tilAmount.error = null
        }

        if (actvCategory.text.isNullOrEmpty()) {
            tilCategory.error = "Category is required"
            valid = false
        } else {
            tilCategory.error = null
        }

        return valid
    }

    private fun onSaveClicked() {
        if (!validateInputs()) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = etAmount.text.toString().trim().toDouble()
        val category = actvCategory.text.toString().trim()
        val note = etNote.text.toString().trim()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_save))
            .setMessage("Save PKR ${String.format("%.0f", amount)} for $category?")
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                saveExpense(amount, category, note)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun saveExpense(amount: Double, category: String, note: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(System.currentTimeMillis())

        if (editExpenseId != -1) {
            val updated = Expense(editExpenseId, amount, category, note, today)
            DataManager.updateExpense(applicationContext, updated)
        } else {
            val newExpense = Expense(0, amount, category, note, today)
            DataManager.addExpense(applicationContext, newExpense)
        }

        sendBroadcast(Intent(DataUpdateReceiver.ACTION_DATA_UPDATED))
        Toast.makeText(this, getString(R.string.expense_saved), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
