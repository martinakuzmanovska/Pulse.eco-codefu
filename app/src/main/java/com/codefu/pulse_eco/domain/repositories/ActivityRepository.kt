package com.codefu.pulse_eco.domain.repositories

import com.codefu.pulse_eco.domain.models.Activity


interface ActivityRepository {
    suspend fun fetchActivities(): List<Activity>
    suspend fun createActivities(activityName: String,points:Int,description:String,onComplete: (Boolean) -> Unit)


}