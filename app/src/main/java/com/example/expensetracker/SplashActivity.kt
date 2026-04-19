package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.utils.DataManager

class SplashActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvLoadingText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.splashProgressBar)
        tvLoadingText = findViewById(R.id.tvLoadingText)

        DataManager.loadAll(applicationContext)

        startProgress()
    }

    private fun startProgress() {
        val steps = listOf(
            "Loading expenses…" to 30,
            "Loading profile…" to 60,
            "Setting up dashboard…" to 85,
            "Ready!" to 100
        )
        var stepIndex = 0

        fun nextStep() {
            if (stepIndex >= steps.size) {
                navigateToHome()
                return
            }
            val (msg, target) = steps[stepIndex++]
            animateTo(target, msg) { nextStep() }
        }

        nextStep()
    }

    private fun animateTo(target: Int, message: String, onDone: () -> Unit) {
        tvLoadingText.text = message
        val runnable = object : Runnable {
            override fun run() {
                if (progress < target) {
                    progress++
                    progressBar.progress = progress
                    handler.postDelayed(this, 12L)
                } else {
                    handler.postDelayed({ onDone() }, 150L)
                }
            }
        }
        handler.post(runnable)
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
