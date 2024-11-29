package com.codefu.pulse_eco.domain.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Event(
    override val activityName: String = "",
    override val points: Int = 0,
    override val description: String = "",
    override val type: String = "",
    var date: String = "",
    var averageValue: Double = 0.0,
    var qrCodeString: String = ""
) : Activity(activityName, points, description, type="event")
