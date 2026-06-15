package com.example.guardianfall.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.guardianfall.MainActivity
import com.example.guardianfall.R
import kotlin.math.sqrt

class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    // Balanced thresholds for real-world falls
    private val IMPACT_THRESHOLD = 2.8 * 9.81 // ~2.8g
    private val ROTATION_THRESHOLD = 2.5 // rad/s
    private val STILLNESS_THRESHOLD = 1.2 * 9.81 // g (Device should be steady)
    private val WINDOW_MS = 2000 // Time window to combine impact + rotation

    private var impactDetected = false
    private var rotationDetected = false
    private var lastImpactTime: Long = 0
    private var lastRotationTime: Long = 0

    companion object {
        const val CHANNEL_ID = "FallDetectionChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_FALL_DETECTED = "com.example.guardianfall.ACTION_FALL_DETECTED"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        registerSensors()
        Log.i("FallDetector", "Service Started - Monitoring for falls...")
    }

    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure service is restarted if killed by system
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val currentTime = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                
                if (mag > IMPACT_THRESHOLD) {
                    Log.i("FallDetector", "Sudden Impact: ${mag / 9.81f}g")
                    impactDetected = true
                    lastImpactTime = currentTime
                }
                
                // If impact + rotation happened, check for stillness to confirm
                if (impactDetected && rotationDetected) {
                    val timeSinceImpact = currentTime - lastImpactTime
                    val timeSinceRotation = currentTime - lastRotationTime
                    
                    if (timeSinceImpact < WINDOW_MS && timeSinceRotation < WINDOW_MS) {
                        // Very basic stillness check: magnitude near 1g (9.81)
                        if (mag < STILLNESS_THRESHOLD) {
                            triggerFallAlert()
                        }
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val rotationMag = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                if (rotationMag > ROTATION_THRESHOLD) {
                    Log.i("FallDetector", "Sudden Rotation: $rotationMag rad/s")
                    rotationDetected = true
                    lastRotationTime = currentTime
                }
            }
        }
        
        // Reset flags after window expires
        if (impactDetected && (currentTime - lastImpactTime > WINDOW_MS)) {
            impactDetected = false
        }
        if (rotationDetected && (currentTime - lastRotationTime > WINDOW_MS)) {
            rotationDetected = false
        }
    }

    private fun triggerFallAlert() {
        Log.e("FallDetector", "!!! FALL CONFIRMED !!!")
        impactDetected = false
        rotationDetected = false
        
        val intent = Intent(ACTION_FALL_DETECTED).apply {
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Fall Detection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GuardianFall Active")
            .setContentText("Monitoring for falls...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
