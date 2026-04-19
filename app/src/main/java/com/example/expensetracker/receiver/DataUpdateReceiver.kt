package com.example.expensetracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DataUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DATA_UPDATED -> {
                Log.d(TAG, "Data updated broadcast received")
            }
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "Screen turned on")
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "User unlocked device")
            }
        }
    }

    companion object {
        const val ACTION_DATA_UPDATED = "com.example.expensetracker.DATA_UPDATED"
        private const val TAG = "DataUpdateReceiver"
    }
}
