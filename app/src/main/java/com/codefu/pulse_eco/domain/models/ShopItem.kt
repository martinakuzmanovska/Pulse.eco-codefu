package com.codefu.pulse_eco.domain.models

data class ShopItem(
    val name:String?="",
    val pointsRequired:Int=0,
    val description:String?="",
    val url:String?=""
)


//open val activityName: String? = "",
//    open val points: Int? = 0,
//    open val description: String? = "",
//    open val type: String? = "activity"