package com.codefu.pulse_eco

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codefu.pulse_eco.workers.WeeklyPulseEcoWorker
import java.util.concurrent.TimeUnit


class PulseEcoApp: Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleWeeklyDataRetrieval(applicationContext)
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


fun testWorker(context: Context) {
    val testWorkRequest = OneTimeWorkRequestBuilder<WeeklyPulseEcoWorker>().build()
    WorkManager.getInstance(context).enqueue(testWorkRequest)
}
