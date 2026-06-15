package com.example.guardianfall

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.guardianfall.data.EmergencyManager
import com.example.guardianfall.data.LocationManager
import com.example.guardianfall.service.FallDetectionService
import com.example.guardianfall.ui.DashboardScreen
import com.example.guardianfall.ui.FallAlertScreen
import com.example.guardianfall.ui.theme.GuardianFallTheme

class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var emergencyManager: EmergencyManager

    private val fallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == FallDetectionService.ACTION_FALL_DETECTED) {
                isFallDetected = true
            }
        }
    }

    private var isFallDetected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        locationManager = LocationManager(this)
        emergencyManager = EmergencyManager(this)

        val filter = IntentFilter(FallDetectionService.ACTION_FALL_DETECTED)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(fallReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(fallReceiver, filter)
        }

        setContent {
            GuardianFallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val permissions = mutableListOf(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ).apply {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            add(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }.toTypedArray()

                    var hasPermissions by remember {
                        mutableStateOf(permissions.all {
                            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                        })
                    }

                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { result ->
                        hasPermissions = result.values.all { it }
                        if (hasPermissions) {
                            startFallService()
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (!hasPermissions) {
                            launcher.launch(permissions)
                        } else {
                            startFallService()
                        }
                    }

                    if (isFallDetected) {
                        FallAlertScreen(
                            onCancel = { isFallDetected = false },
                            onConfirm = {
                                triggerEmergencyAlert()
                                isFallDetected = false
                            }
                        )
                    } else {
                        DashboardScreen()
                    }
                }
            }
        }
    }

    private fun startFallService() {
        val intent = Intent(this, FallDetectionService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun triggerEmergencyAlert() {
        val sharedPreferences = getSharedPreferences("GuardianFallPrefs", Context.MODE_PRIVATE)
        val phoneNumber = sharedPreferences.getString("emergency_contact", "") ?: ""
        
        android.util.Log.d("GuardianFall", "Triggering alert for $phoneNumber")
        
        if (phoneNumber.isNotEmpty()) {
            // Default location for emulator testing
            val defaultLocation = "https://maps.google.com/?q=12.9716,77.5946"
            
            locationManager.getCurrentLocation { location ->
                android.util.Log.d("GuardianFall", "Location received: $location")
                val locationUrl = if (location != null) {
                    "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    // Use default location instead of "Location unknown" for demo/emulator purposes
                    defaultLocation
                }
                emergencyManager.sendEmergencySms(phoneNumber, locationUrl)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(fallReceiver)
    }
}
