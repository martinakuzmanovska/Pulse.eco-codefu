package com.codefu.pulse_eco.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmReceiver:  BroadcastReceiver() {
    override  fun onReceive(context: Context?, intent: Intent?) {

        val sharedPreferences = context?.getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
        val steps = sharedPreferences?.getInt("steps", 0)
        val userActivityLogRepository:UserActivityLogRepository=UserActivityLogRepositoryImpl()
        if (steps != null && steps >= 5000) {
            CoroutineScope(Dispatchers.IO).launch {
                userActivityLogRepository.createLog("Steps Taken", "Today's steps", (0.001 * steps).toInt()) { success ->
                    if (success) {
                        println("Sent to database")
                    } else {
                        println("Unsuccessful")
                    }
                }
            }
        }


    }
}