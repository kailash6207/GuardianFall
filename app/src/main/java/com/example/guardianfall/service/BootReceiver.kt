package com.example.guardianfall.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            android.util.Log.i("BootReceiver", "Device rebooted. Starting GuardianFall Service...")
            
            val serviceIntent = Intent(context, FallDetectionService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
