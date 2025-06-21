package com.codefu.pulse_eco.domain.repositories

import com.codefu.pulse_eco.domain.models.UserActivityLog

interface UserActivityLogRepository {
    suspend fun fetchUserLogs(userId: String): List<UserActivityLog>
    suspend fun createLog(activityId: String, onComplete: (Boolean) -> Unit)
}
