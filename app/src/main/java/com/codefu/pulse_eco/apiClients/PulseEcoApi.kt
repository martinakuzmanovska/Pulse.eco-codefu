package com.codefu.pulse_eco.apiClients

import com.codefu.pulse_eco.apiClients.dataModels.SensorsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PulseEcoApi {
    @GET("data24h")
    suspend fun getAllPulseEcoSensors() : List<SensorsResponse>

    @GET("dataRaw")
    suspend fun getPM10DataFromSensorId(
        @Query("sensorId") sensorId: String,
        @Query("type") type: String = "pm10",
        @Query("from") from: String,
        @Query("to") to: String
    ): List<SensorsResponse>

}