package com.example.guardianfall.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import java.net.URLEncoder

class EmergencyManager(private val context: Context) {

    fun sendEmergencySms(phoneNumber: String, locationUrl: String) {
        try {
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
            val message = "!!! EMERGENCY !!! Possible fall detected! Location: $locationUrl"
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("EmergencyManager", "SMS sent to $phoneNumber")
            
            // Also attempt to send via WhatsApp (Requires one user click)
            sendWhatsAppMessage(phoneNumber, message)
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Failed to send SMS", e)
        }
    }

    private fun sendWhatsAppMessage(phoneNumber: String, message: String) {
        try {
            // Clean phone number (must be in international format without +)
            val cleanNumber = phoneNumber.replace("+", "").replace(" ", "")
            val packageManager = context.packageManager
            val i = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=" + URLEncoder.encode(message, "UTF-8")
            
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            if (i.resolveActivity(packageManager) != null) {
                context.startActivity(i)
                Log.d("EmergencyManager", "WhatsApp opened for $phoneNumber")
            }
        } catch (e: Exception) {
            Log.e("EmergencyManager", "WhatsApp not installed or failed", e)
        }
    }
}
