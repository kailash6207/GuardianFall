package com.example.guardianfall.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        // First try to get the very last known location (fastest)
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
            if (lastLoc != null && (System.currentTimeMillis() - lastLoc.time) < 60000) {
                // Last location is fresh enough (less than 1 minute old)
                onLocationReceived(lastLoc)
            } else {
                // Request a fresh location update
                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000
                ).setMaxUpdates(1).build()

                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location: Location? ->
                    onLocationReceived(location)
                }.addOnFailureListener {
                    onLocationReceived(null)
                }
            }
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    }
}
