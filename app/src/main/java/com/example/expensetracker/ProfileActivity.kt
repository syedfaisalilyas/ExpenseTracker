package com.example.expensetracker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.expensetracker.model.UserProfile
import com.example.expensetracker.utils.DataManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvProfileName: TextView
    private lateinit var tilName: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var tilBudget: TextInputLayout
    private lateinit var etBudget: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvProfileName = findViewById(R.id.tvProfileName)
        tilName = findViewById(R.id.tilName)
        etName = findViewById(R.id.etName)
        tilBudget = findViewById(R.id.tilBudget)
        etBudget = findViewById(R.id.etBudget)

        loadProfileData()
        setupFocusListeners()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveProfile)
            .setOnClickListener { onSaveClicked() }
    }

    override fun onStart() {
        super.onStart()
        DataManager.loadProfile(applicationContext)
        loadProfileData()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        loadProfileData()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadProfileData() {
        val profile = DataManager.getProfile()
        etName.setText(profile.name)
        if (profile.monthlyBudget > 0) {
            etBudget.setText(profile.monthlyBudget.toLong().toString())
        }
        tvProfileName.text = profile.name.ifEmpty { "Your Name" }
    }

    private fun setupFocusListeners() {
        etName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    tilName.error = "Name cannot be empty"
                } else {
                    tilName.error = null
                    tvProfileName.text = name
                }
            }
        }

        etBudget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val budgetStr = etBudget.text.toString().trim()
                if (budgetStr.isNotEmpty() && (budgetStr.toDoubleOrNull() == null || budgetStr.toDouble() < 0)) {
                    tilBudget.error = "Enter a valid budget"
                } else {
                    tilBudget.error = null
                }
            }
        }
    }

    private fun onSaveClicked() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            tilName.error = "Name is required"
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetStr = etBudget.text.toString().trim()
        val budget = if (budgetStr.isEmpty()) 0.0 else budgetStr.toDoubleOrNull() ?: 0.0

        val currentProfile = DataManager.getProfile()
        val updated = UserProfile(name = name, monthlyBudget = budget, weeklyTarget = currentProfile.weeklyTarget)
        DataManager.saveProfile(applicationContext, updated)

        tvProfileName.text = name
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
