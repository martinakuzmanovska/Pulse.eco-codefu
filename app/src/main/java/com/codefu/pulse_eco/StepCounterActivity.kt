package com.codefu.pulse_eco

import StepCounterViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataPoint
import com.google.android.gms.fitness.data.LocalDataSet
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.request.LocalDataReadRequest
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class StepCounterActivity : AppCompatActivity() {
    private  var steps:Int = 0
    private lateinit var stepCounterViewModel: StepCounterViewModel

    companion object {
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001
    }

    val timer = Timer()


    private lateinit var numberOfStepsTextView:TextView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.stepcounterlayout)


        numberOfStepsTextView=findViewById(R.id.stepCountTextView)
        stepCounterViewModel=ViewModelProvider(this)[StepCounterViewModel::class.java]
        val localRecordingClient=FitnessLocal.getLocalRecordingClient(this)

        val hasMinPlayServices = isGooglePlayServicesAvailable(this, LocalRecordingClient.LOCAL_RECORDING_CLIENT_MIN_VERSION_CODE)

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

        timer.schedule(object : TimerTask() {
            override fun run() {
                readAndProcessFitnessData(localRecordingClient)
            }
        }, 0, 100)


    }

    override fun onResume() {
        super.onResume()
        val localRecordingClient=FitnessLocal.getLocalRecordingClient(this)
        readAndProcessFitnessData(localRecordingClient)
        timer.schedule(object : TimerTask() {
            override fun run() {
                readAndProcessFitnessData(localRecordingClient)
            }
        }, 0, 500)


    }
    private fun dumpDataSet(dataSet: LocalDataSet) {
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            Log.i(TAG,"Data point:")
            Log.i(TAG,"\tType: ${dp.dataType.name}")
            Log.i(TAG,"\tStart: ${dp.getStartTime(TimeUnit.HOURS)}")
            Log.i(TAG,"\tEnd: ${dp.getEndTime(TimeUnit.HOURS)}")
            for (field in dp.dataType.fields) {
                Log.i(TAG,"\tLocalField: ${field.name} LocalValue: ${dp.getValue(field)}")
                numberOfStepsTextView.text=dp.getValue(field).toString()


            }
        }
    }
    private fun readAndProcessFitnessData(localRecordingClient: LocalRecordingClient)
    {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)
        val readRequest =
            LocalDataReadRequest.Builder()
                // The data request can specify multiple data types to return,
                // effectively combining multiple data queries into one call.
                // This example demonstrates aggregating only one data type.
                .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be
                // aggregated. bucketByTime allows bucketing by time span.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        localRecordingClient.readData(readRequest).addOnSuccessListener { response ->
            // The aggregate query puts datasets into buckets, so flatten into a
            // single list of datasets.
            for (dataSet in response.buckets.flatMap { it.dataSets }) {
                dumpDataSet(dataSet)
            }
        }
            .addOnFailureListener { e ->
                Log.w(TAG,"There was an error reading data", e)
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



}