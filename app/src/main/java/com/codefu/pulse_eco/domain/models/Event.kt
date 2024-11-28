package com.codefu.pulse_eco.domain.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Event(
    override val id: Int = 0,
    override val activityName: String = "",
    override val points: Int = 0,
    override val description: String = "",
    val date: String = "",
    val qrCodeString: String = ""
) : Activity(id, activityName, points, description)