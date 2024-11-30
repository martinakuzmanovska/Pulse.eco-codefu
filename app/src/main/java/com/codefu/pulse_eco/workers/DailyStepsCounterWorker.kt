package com.codefu.pulse_eco.workers

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.codefu.pulse_eco.StepCounterActivity
import com.codefu.pulse_eco.apiClients.PulseEcoApiProvider
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.runBlocking

class DailyStepsCounterWorker (private val appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams){
    override fun doWork(): Result {

        val userActivityLogRepository:UserActivityLogRepository=UserActivityLogRepositoryImpl(appContext)
        return try{
            runBlocking {
                val sharedPref =
                    applicationContext.getSharedPreferences("StepsTaken", Context.MODE_PRIVATE)
                val steps = sharedPref.getInt("StepCount", 0)
                if (steps >= 5000) {
                    val points=(steps*0.001).toInt()
                    userActivityLogRepository.createLog("StepsTaken", "Steps taken today", points){
                        success ->
                        if (success) {
                            println("Activity Log  created successfully!")
                        } else {
                            println("Failed to create activity log.")
                        }
                    }
                }
            }
            Result.success()
        }
        catch (e:Exception){
            e.printStackTrace()
            Result.retry()
        }
    }
}