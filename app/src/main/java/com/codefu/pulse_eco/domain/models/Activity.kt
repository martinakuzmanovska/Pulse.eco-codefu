package com.codefu.pulse_eco.domain.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
open class Activity(
    open val activityName: String? = "",
    open val points: Int? = 0,
    open val description: String? = "",
    open val type: String? = "activity"
)
