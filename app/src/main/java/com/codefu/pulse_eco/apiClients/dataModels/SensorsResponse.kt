package com.codefu.pulse_eco.apiClients.dataModels

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class SensorsResponse (
    val sensorId: String = "",
    val stamp: String = "",
    val type: String = "",
    val position: String = "",
    val value: String = "",
)

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0  // Radius of the Earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c  // Distance in kilometers
}

fun findClosestSensor(targetLat: Double, targetLon: Double, sensors: List<SensorsResponse>): SensorsResponse? {
    var closestSensor: SensorsResponse? = null
    var minDistance = Double.MAX_VALUE

    for (sensor in sensors) {
        val (latStr, lonStr) = sensor.position.split(",")
        val lat = latStr.toDoubleOrNull() ?: continue
        val lon = lonStr.toDoubleOrNull() ?: continue

        val distance = haversine(targetLat, targetLon, lat, lon)
        if (distance < minDistance) {
            minDistance = distance
            closestSensor = sensor
        }
    }
    return closestSensor
}