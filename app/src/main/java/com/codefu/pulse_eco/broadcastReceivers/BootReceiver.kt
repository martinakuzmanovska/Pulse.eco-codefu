package com.codefu.pulse_eco.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.codefu.pulse_eco.service.StepCounterService


class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED){
            val serviceIntent = Intent(context, StepCounterService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}