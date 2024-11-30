package com.codefu.pulse_eco.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase


class AlarmReceiver:  BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val sharedPreferences = context?.getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
        val steps = sharedPreferences?.getInt("steps", 0)
        val database = FirebaseDatabase.getInstance()
        //TODO Create as log and then send to database

    }
}