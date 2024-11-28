package com.codefu.pulse_eco.domain.repositories

import com.codefu.pulse_eco.domain.models.Activity


interface ActivityRepository {
    suspend fun fetchActivities(): List<Activity>
    suspend fun createActivities(activity: Activity,onComplete: (Boolean) -> Unit)

}