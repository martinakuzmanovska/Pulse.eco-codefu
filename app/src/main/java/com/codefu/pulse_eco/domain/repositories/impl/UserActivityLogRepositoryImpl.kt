package com.codefu.pulse_eco.domain.repositories.impl

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.models.UserActivityLog
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_REF
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_USER_LOGS
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class UserActivityLogRepositoryImpl
    (context: Context,
     private val rootRefDB: DatabaseReference = Firebase.database.reference,
     private val userActivityLogRef: DatabaseReference = rootRefDB.child(ACTIVITY_USER_LOGS),
     private var activitiesRef: DatabaseReference = rootRefDB.child(ACTIVITY_REF)) : UserActivityLogRepository
{
    private val googleAuthUiClient:GoogleAuthUiClient=GoogleAuthUiClient(context,
        oneTapClient = Identity.getSignInClient(context))
        private var userActivityLogListener: ValueEventListener? = null

    override suspend fun fetchUserLogs(userId: String): List<UserActivityLog> {
       return suspendCoroutine { continuation ->
           userActivityLogListener = object : ValueEventListener {
               override fun onDataChange(snapshot: DataSnapshot) {
                   try {
                       val logs = snapshot.children
                           .filter { it.key == userId }
                           .flatMap { user ->
                               user.children.mapNotNull {
                                   child -> child.getValue(UserActivityLog::class.java)
                               }
                           }

                       val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

                       val sortedLogs = logs.sortedBy {
                           LocalDate.parse(it.date, dateTimeFormatter)
                       }

                       Log.d("Firebase", "Raw Data: ${snapshot.value}")
                       continuation.resume(sortedLogs)

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

    override suspend fun createLog(
        activityId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val userId = googleAuthUiClient.getSignedInUser()?.userId
        if (userId == null) {
            Log.e("ActivityLOG", "User ID is null, cannot log activity.")
            onComplete(false)
            return
        }

        val activityLogsRef = userActivityLogRef.child(userId)
        val scannedActivityRef = activitiesRef.child(activityId)

        scannedActivityRef.get()
            .addOnSuccessListener { snapshot ->
                val scannedActivity: Activity? = snapshot.getValue(Activity::class.java)

                if (scannedActivity != null){
                    val log = UserActivityLog(
                        userId = userId,
                        activityName = scannedActivity.activityName.toString(),
                        date = getDateTime(),
                        points = scannedActivity.points,
                        description = scannedActivity.description
                    )

                    activityLogsRef.push()
                        .setValue(log)
                        .addOnSuccessListener {
                            Log.i("ActivityLOG", "User activity logged successfully")
                            onComplete(true)
                        }
                        .addOnFailureListener {
                                e -> Log.e("ActivityLOG", "Failed to log user activity", e)
                            onComplete(false)
                        }
                }
                else{
                    Log.e("ActivityLOG", "Activity not found for ID: $activityId")
                    onComplete(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ActivityLOG", "Failed to fetch activity", e)
                onComplete(false)
            }
    }

    @SuppressLint("DefaultLocale")
    private fun getDateTime() :String{
        val date: ZonedDateTime = LocalDateTime.now().atZone(
            ZoneId.systemDefault()
        )
        val year: Int = date.year
        val month: Int = date.monthValue
        val day: Int = date.dayOfMonth
        val result = String.format(
            "%02d-%02d-%d %d:%d",
            day, month, year,
            date.hour, date.minute
        )
        return result
    }

    fun detachUserActivityLogListener() {
        userActivityLogListener.let {
            if (it != null) {
                userActivityLogRef.removeEventListener(it)
                userActivityLogListener = null
            }

        }
    }

}