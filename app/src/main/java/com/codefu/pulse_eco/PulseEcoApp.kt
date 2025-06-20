package com.codefu.pulse_eco

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codefu.pulse_eco.service.StepCounterService
import com.codefu.pulse_eco.workers.StepCounterWorker
import com.codefu.pulse_eco.workers.WeeklyPulseEcoWorker
import java.util.concurrent.TimeUnit


class PulseEcoApp: Application() {
    override fun onCreate() {
        super.onCreate()

        val serviceIntent = Intent(applicationContext, StepCounterService::class.java)
        applicationContext.startForegroundService(serviceIntent)

        scheduleWeeklyDataRetrieval(applicationContext)
        scheduleStepCounterWorker(applicationContext)
    }
}

@SuppressLint("SuspiciousIndentation")
fun scheduleWeeklyDataRetrieval(applicationContext : Context) {
    val workRequest = PeriodicWorkRequestBuilder<WeeklyPulseEcoWorker>(7, TimeUnit.DAYS)
        .build()

    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        "WeeklyPulseEcoWorker",
        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )

    // Comment the statement above and uncomment this one to test the worker - so you won't wait to see if it works :)
    // testWorker(applicationContext)
}

@SuppressLint("SuspiciousIndentation")
fun scheduleStepCounterWorker(applicationContext: Context) {
    val workManager = WorkManager.getInstance(applicationContext)

    // Cancel any existing workers with this unique name
    workManager.cancelUniqueWork("StepCounterWorker")

    val currentTime = System.currentTimeMillis()
    val midnightTime = getNextMidnightTimeInMillis()
    val initialDelay = midnightTime - currentTime

    val workRequest = PeriodicWorkRequestBuilder<StepCounterWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS) //  Start at midnight
        .build()

    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        "StepCounterWorker",
        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}


fun testWorker(context: Context) {
    val testWorkRequest = OneTimeWorkRequestBuilder<StepCounterWorker>().build()
    WorkManager.getInstance(context).enqueue(testWorkRequest)
}

fun getNextMidnightTimeInMillis(): Long {
    val now = java.util.Calendar.getInstance()
    now.add(java.util.Calendar.DAY_OF_YEAR, 1) // Move to next day
    now.set(java.util.Calendar.HOUR_OF_DAY, 0)
    now.set(java.util.Calendar.MINUTE, 0)
    now.set(java.util.Calendar.SECOND, 0)
    now.set(java.util.Calendar.MILLISECOND, 0)
    return now.timeInMillis
}
