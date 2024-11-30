package com.codefu.pulse_eco.domain.repositories.impl

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.codefu.pulse_eco.domain.models.UserActivityLog
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_USER_LOGS
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class UserActivityLogRepositoryImpl (
    context: Context ,
    private val rootRefDB: DatabaseReference = Firebase.database.reference,
    private val  userActivityLogRef: DatabaseReference = rootRefDB.child(ACTIVITY_USER_LOGS),
    )
    : UserActivityLogRepository{

//        private val applicationContext: Context = getApplicationContext()
    private val googleAuthUiClient:GoogleAuthUiClient=GoogleAuthUiClient(context,
        oneTapClient = Identity.getSignInClient(context))
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


    override suspend fun createLog(
        activityName: String,
        description: String,
        points: Int,
        onComplete: (Boolean) -> Unit
    ) {
        val user=googleAuthUiClient.getSignedInUser()
        val userActivityLog:UserActivityLog=UserActivityLog(user?.userId,activityName,
            LocalDateTime.now().atZone(
                ZoneId.systemDefault()).toString(),
            description,
            points
        )
        userActivityLogRef.push().setValue(userActivityLog)

    }

    fun detachUserActivityLogListener() {
        userActivityLogListener.let {
            if (it != null) {
                userActivityLogRef.removeEventListener(it)
                userActivityLogListener = null
            }

        }
    }
//    open val userId: String? = "",
//    open val activityId: Int? = 0,
//    open val date: String? = "",
//    open val description: String? = "",
//    open val points: Int? = 0




}