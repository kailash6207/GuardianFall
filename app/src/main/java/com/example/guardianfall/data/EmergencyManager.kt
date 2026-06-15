package com.example.guardianfall.data

import android.content.Context
import android.telephony.SmsManager
import android.util.Log

class EmergencyManager(private val context: Context) {

    fun sendEmergencySms(phoneNumber: String, locationUrl: String) {
        try {
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
            val message = "Possible fall detected! Location: $locationUrl"
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("EmergencyManager", "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Failed to send SMS", e)
        }
    }
}
