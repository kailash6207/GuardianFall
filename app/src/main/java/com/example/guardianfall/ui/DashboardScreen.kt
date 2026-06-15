package com.example.guardianfall.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("GuardianFallPrefs", Context.MODE_PRIVATE) }
    
    var phoneNumber by remember { mutableStateOf(sharedPreferences.getString("emergency_contact", "") ?: "") }
    var isMonitoring by remember { mutableStateOf(true) } // Start true by default

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "GuardianFall Dashboard", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it
                sharedPreferences.edit().putString("emergency_contact", it).apply()
            },
            label = { Text("Emergency Contact Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { isMonitoring = !isMonitoring },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isMonitoring) "Stop Monitoring" else "Start Monitoring")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isMonitoring) {
            Text(text = "Monitoring active...", color = MaterialTheme.colorScheme.primary)
        }
    }
}
