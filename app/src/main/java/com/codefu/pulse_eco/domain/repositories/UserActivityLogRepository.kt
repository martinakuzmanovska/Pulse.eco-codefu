package com.codefu.pulse_eco.domain.repositories

import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.models.UserActivityLog

interface UserActivityLogRepository {
    suspend fun fetchUserLogs(userId: String): List<UserActivityLog>
   suspend fun createLog(userId: String,
                         activityName: String,
                         date: String,
                         description: String,
                         points: Int,
                         onComplete: (Boolean) -> Unit)


//      open val userId: String? = "",
//        open val activityName: String? = 0,
//        open val date: String? = "",
//        open val description: String? = "",
//        open val points: Int? = 0
}