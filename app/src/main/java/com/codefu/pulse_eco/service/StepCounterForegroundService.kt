package com.codefu.pulse_eco.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var totalSteps = 0

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        Log.i("StepCount", "On create")
        // Initialize the step sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "Step Counter Sensor not available", Toast.LENGTH_LONG).show()
            stopSelf()
        }

        createNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, "StepCounterServiceChannel")
            .setContentTitle("Step Counter Service")
            .setContentText("Tracking your steps")
            .build()

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("StepCount", "On start")
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.i("StepService", "onSensorChanged")
        if (event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val sharedPreferences = getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
            val offset = sharedPreferences.getInt("offset", 0)

            totalSteps = event.values[0].toInt()
            val steps = totalSteps - offset
            Log.d("StepCount", "Steps: $steps")

            sharedPreferences.edit()
                .putInt("steps", steps)
                .putInt("totalSteps", totalSteps)
                .apply()

            val intent = Intent("com.codefu.pulse_eco.STEP_COUNT_UPDATE")
            intent.putExtra("step_count", totalSteps)
            sendBroadcast(intent)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)

        // Optional: Restart if killed
        val serviceIntent = Intent(this, StepCounterService::class.java)
        startForegroundService(serviceIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "StepCounterServiceChannel",
            "Step Counter Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
