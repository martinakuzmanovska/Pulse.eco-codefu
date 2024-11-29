package com.codefu.pulse_eco.domain.repositories.impl

import android.util.Log
import com.codefu.pulse_eco.domain.models.UserActivityLog
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_REF
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_USER_LOGS
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserActivityLogRepositoryImpl (
    private val rootRefDB: DatabaseReference = Firebase.database.reference,
    private val  userActivityLogRef: DatabaseReference = rootRefDB.child(ACTIVITY_USER_LOGS))
    : UserActivityLogRepository{

        private var userActivityLogListener: ValueEventListener? = null

    override suspend fun fetchUserLogs(userId: String): List<UserActivityLog> {
       return suspendCoroutine { continuation ->
           userActivityLogListener = object : ValueEventListener {
               override fun onDataChange(snapshot: DataSnapshot) {
                   try {
                       val logs = snapshot.children.mapNotNull { 
                           child ->
                           val log = child.getValue(UserActivityLog::class.java)
                           if(log?.userId == userId) log else null
                       }
                       Log.d("Firebase", "Raw Data: ${snapshot.value}")
                       continuation.resume(logs)

                   } catch (e: Exception) {
                       Log.e("Firebase", "Error parsing data", e)
                       continuation.resume(emptyList())
                   }
               }

               override fun onCancelled(error: DatabaseError) {
                   Log.e("firebase", "Database error", error.toException())
                   continuation.resume(emptyList())
               }
           }

           userActivityLogRef.addListenerForSingleValueEvent(userActivityLogListener!!)

       }
    }

    fun detachUserActivityLogListener() {
        userActivityLogListener.let {
            if (it != null) {
                userActivityLogRef.removeEventListener(it)
                userActivityLogListener = null
            }

        }
    }

    override suspend fun createLog(
        userActivityLog: UserActivityLog,
        onComplete: (Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}