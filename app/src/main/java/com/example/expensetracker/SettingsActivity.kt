package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.expensetracker.receiver.DataUpdateReceiver
import com.example.expensetracker.utils.DataManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
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
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupClickListeners() {
        findViewById<android.view.View>(R.id.rowAbout).setOnClickListener {
            showAboutDialog()
        }

        findViewById<android.view.View>(R.id.rowReset).setOnClickListener {
            showResetConfirmDialog()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Expense Tracker")
            .setMessage(getString(R.string.about_text))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset All Data")
            .setMessage(getString(R.string.reset_confirm))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes, Reset") { _, _ ->
                performReset()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun performReset() {
        DataManager.resetAllData(applicationContext)
        sendBroadcast(Intent(DataUpdateReceiver.ACTION_DATA_UPDATED))
        Toast.makeText(this, getString(R.string.data_reset), Toast.LENGTH_LONG).show()
    }
}
