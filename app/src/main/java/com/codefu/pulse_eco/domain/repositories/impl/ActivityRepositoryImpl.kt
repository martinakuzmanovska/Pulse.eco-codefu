package com.codefu.pulse_eco.domain.repositories.impl

import android.util.Log
import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.repositories.ActivityRepository
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_REF
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ActivityRepositoryImpl(
    private val rootRefDB: DatabaseReference = Firebase.database.reference,
    private val activitiesRef: DatabaseReference = rootRefDB.child(ACTIVITY_REF)
) : ActivityRepository {

    private var activityListener: ValueEventListener? = null

//    override suspend fun fetchActivities(): List<Activity> {
//        return try {
//            val dataSnapshot = activitiesRef.get().await()
//           dataSnapshot.children.mapNotNull { it.getValue(Activity::class.java) }
//
//        } catch (exception: Exception) {
//            Log.e("firebase", "Error getting data", exception)
//            emptyList()
//        }
//    }

    override suspend fun fetchActivities(): List<Activity> {
        return suspendCoroutine { continuation ->
            activityListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val activities = dataSnapshot.children.mapNotNull { it.getValue(Activity::class.java) }
                        Log.d("FirebaseData", "Raw Data: ${dataSnapshot.value}")
                        continuation.resume(activities)
                    } catch (e: Exception) {
                        Log.e("firebase", "Error parsing data", e)
                        continuation.resume(emptyList())
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("firebase", "Database error", databaseError.toException())
                    continuation.resume(emptyList())
                }
            }

            activitiesRef.addListenerForSingleValueEvent(activityListener!!)
        }
    }

    fun detachActivityListener() {
        activityListener?.let {
            activitiesRef.removeEventListener(it) // Remove the listener
            activityListener = null // Clear the reference
        }
    }
//open val id: Int = 0,
//    v

    override suspend fun createActivities(
        activityName: String,
        points: Int,
        description: String,
        onComplete: (Boolean) -> Unit
    ) {
        val activity = Activity(activityName, points, description)
        activitiesRef.push().setValue(activity)
            .addOnSuccessListener {
                onComplete(true) // Notify success
            }
            .addOnFailureListener { exception ->
                println("Error adding activity: ${exception.message}")
                onComplete(false) // Notify failure
            }

    }
}
