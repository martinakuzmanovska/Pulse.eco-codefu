package com.codefu.pulse_eco.apiClients.dataModels

data class SensorsResponse (
    val sensorId: String = "",     // The unique ID of the sensor.
    val position: String = "",     // "Latidide longitute" GPS coordinates
    val type: String = "",         // The type ID of the sensor
    val description: String = "",  // Short decription / name
    val comments: String = "",     // Any other comments
    val status: String = ""
)