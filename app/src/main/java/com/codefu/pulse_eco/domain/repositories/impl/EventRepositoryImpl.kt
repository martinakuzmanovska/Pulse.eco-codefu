package com.codefu.pulse_eco.domain.repositories.impl

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.models.Event
import com.codefu.pulse_eco.domain.repositories.EventRepository
import com.codefu.pulse_eco.qrcode.QrCodeGenerator
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_REF
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EventRepositoryImpl(
    private val rootRefDB: DatabaseReference = Firebase.database.reference,
    private val activitiesRef: DatabaseReference = rootRefDB.child(ACTIVITY_REF), ): EventRepository {
    private val qrCodeGenerator:QrCodeGenerator= QrCodeGenerator()
    private var activityListener: ValueEventListener? = null
    private val eventsQuery: Query = activitiesRef.orderByChild("type").equalTo("event")

    override suspend fun fetchEvents(): List<Event> {
        return suspendCoroutine { continuation ->
            activityListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val events = dataSnapshot.children.mapNotNull { it.getValue(Event::class.java) }
                        Log.d("FirebaseData", "Raw Data: ${dataSnapshot.value}")
                        continuation.resume(events)
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

            eventsQuery.addListenerForSingleValueEvent(activityListener!!)
        }
    }

//    override suspend fun createEvent(
//        eventName: String,
//        points: Int,
//        description: String,
//        date: String,
//        qrCodeString: String
//    ) {
//        val event:Event=Event(eventName,points,description,date,qrCodeString)
//
//    }

    fun detachActivityListener() {
        activityListener?.let {
            activitiesRef.removeEventListener(it)
            activityListener = null
        }
    }

}