package com.codefu.pulse_eco

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codefu.pulse_eco.workers.DailyStepsCounterWorker
import com.codefu.pulse_eco.workers.WeeklyPulseEcoWorker
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataSet
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.request.LocalDataReadRequest
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class StepCounterActivity : AppCompatActivity() {

    // Define a constant for SharedPreferences key
    companion object {
        private const val STEP_COUNT_KEY = "StepCount"
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001
    }

    private lateinit var numberOfStepsTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stepcounterlayout)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("StepsTaken", Context.MODE_PRIVATE)

        numberOfStepsTextView = findViewById(R.id.stepCountTextView)
        val localRecordingClient = FitnessLocal.getLocalRecordingClient(this)

        val hasMinPlayServices = isGooglePlayServicesAvailable(
            this,
            LocalRecordingClient.LOCAL_RECORDING_CLIENT_MIN_VERSION_CODE
        )

        // Check Google Play Services availability
        if (hasMinPlayServices != ConnectionResult.SUCCESS) {
            return
        }

        // Check and request permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_ACTIVITY_RECOGNITION
            )
        } else {
            // Permission already granted
            subscribeToStepCount()
        }
        readAndProcessFitnessData(localRecordingClient)
    }

    private fun dumpDataSet(dataSet: LocalDataSet): String {
        var steps = "0"
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            Log.i(TAG, "Data point:")
            Log.i(TAG, "\tType: ${dp.dataType.name}")
            Log.i(TAG, "\tStart: ${dp.getStartTime(TimeUnit.HOURS)}")
            Log.i(TAG, "\tEnd: ${dp.getEndTime(TimeUnit.HOURS)}")
            for (field in dp.dataType.fields) {
                Log.i(TAG, "\tLocalField: ${field.name} LocalValue: ${dp.getValue(field)}")
                numberOfStepsTextView.text = dp.getValue(field).toString()
                steps = dp.getValue(field).toString()

                // Save steps count in SharedPreferences
                saveStepsToPreferences(steps)
            }
        }
        return steps
    }

    // Method to save steps count to SharedPreferences
    private fun saveStepsToPreferences(steps: String) {
        val editor = sharedPreferences.edit()
        editor.putInt(STEP_COUNT_KEY, steps.toInt())
        editor.apply()  // Apply changes asynchronously
    }

    private fun readAndProcessFitnessData(localRecordingClient: LocalRecordingClient) {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)
        val readRequest =
            LocalDataReadRequest.Builder()
                .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        localRecordingClient.readData(readRequest).addOnSuccessListener { response ->
            for (dataSet in response.buckets.flatMap { it.dataSets }) {
                dumpDataSet(dataSet)
            }
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was an error reading data", e)
            }
    }


    @SuppressLint("MissingPermission")
    private fun subscribeToStepCount() {
        val localRecordingClient = FitnessLocal.getLocalRecordingClient(this)
// Subscribe to steps data
        localRecordingClient.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully subscribed!")


            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem subscribing.", e)
            }

    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, subscribe to step count
                subscribeToStepCount()
            } else {
                Log.e(TAG, "Activity recognition permission denied.")
            }
        }
    }


    fun scheduleWeeklyDataRetrieval(applicationContext : Context) {

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)

        // Set the calendar time to midnight (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        val workRequest = PeriodicWorkRequestBuilder<DailyStepsCounterWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyStepsCounterWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        // Comment the statement above and uncomment this one to test the worker - so you won't wait to see if it works :)
        // testWorker(applicationContext)
    }

}