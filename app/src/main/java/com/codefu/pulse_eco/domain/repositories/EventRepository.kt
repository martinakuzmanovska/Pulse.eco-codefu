package com.codefu.pulse_eco.domain.repositories

import com.codefu.pulse_eco.domain.models.Event

interface EventRepository {
    suspend fun fetchEvents(): List<Event>
    //    suspend fun createEvent(eventName:String,points:Int,description:String,date:String,qrCodeString:String)

    //override val activityName: String = "",
    //    override val points: Int = 0,
    //    override val description: String = "",
    //    override val type: String = "",
    //    val date: String = "",
    //    val qrCodeString: String = ""
}