package com.codefu.pulse_eco.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.codefu.pulse_eco.domain.models.UserActivityLog
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepCounterWorker(context: Context, workerParameters: WorkerParameters)
    : Worker(context, workerParameters)
{
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun doWork(): Result {
        Log.i("StepWorker", "WORKER")

        val sharedPreferences = applicationContext.getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
        val steps = sharedPreferences.getInt("steps", 0)
        val totalSteps = sharedPreferences.getInt("totalSteps", 0)

        sharedPreferences.edit()
            .putInt("offset", totalSteps)
            .putInt("steps", 0)
            .apply()

        saveToDatabase(steps)

        return Result.success()
    }

    private fun saveToDatabase(steps: Int)
    {
        val db = FirebaseDatabase.getInstance()
        val userId = googleAuthUiClient.getSignedInUser()?.userId

        if (userId == null) {
            Log.e("StepWorker", "User not signed in. Aborting save.")
            return
        }

        val activityLogsRef = db.getReference("activity_user_logs/${userId}")

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val steps_log = UserActivityLog(
            userId = userId,
            activityName = "Daily Steps Count",
            date = currentDate,
            points = 10,
            description = "Daily Steps Count: $steps steps"
        )

        activityLogsRef.push()
            .setValue(steps_log)
            .addOnSuccessListener { Log.i("StepWorker", "Steps uploaded successfully") }
            .addOnFailureListener { e -> Log.e("StepWorker", "Failed to upload steps", e) }
    }
}