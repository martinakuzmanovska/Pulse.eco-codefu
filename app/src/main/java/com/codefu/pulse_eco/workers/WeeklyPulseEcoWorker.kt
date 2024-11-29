package com.codefu.pulse_eco.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.codefu.pulse_eco.apiClients.PulseEcoApi
//import com.codefu.pulse_eco.apiClients.PulseEcoApiClient
import com.codefu.pulse_eco.apiClients.PulseEcoApiProvider
import com.codefu.pulse_eco.domain.models.Event
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_REF
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

class WeeklyPulseEcoWorker(appContext: Context, workerParams: WorkerParameters)
    :Worker(appContext, workerParams){

    override fun doWork(): Result {
        return try{
            runBlocking {
                val api = PulseEcoApiProvider.getPulseEcoApi()
                val event = generateEventFromPulseEco(api)
                saveTreePlantingEventToFirebase(event)
            }
            Result.success()
        }
        catch (e:Exception){
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun saveTreePlantingEventToFirebase(event: Event) {
        val database = FirebaseDatabase.getInstance()
        val activitiesRef = database.reference.child(ACTIVITY_REF)

        val newEventRef = activitiesRef.push()
        newEventRef.setValue(event)
    }

    private suspend fun generateEventFromPulseEco(api: PulseEcoApi): Event {
        val sensors = api.getAllPulseEcoSensors().map { s -> s.sensorId }.distinct()
        val (firstDayFormatted, lastDayFormatted) = formatDateTimeForApi()

        var event : Event
        val averageValuesOfPM10PerSensor = sensors.mapNotNull { sensor ->
            val getPM10Response = api.getPM10DataFromSensorId(sensor, "pm10", firstDayFormatted+"00:00:00+01:00", lastDayFormatted+"02:00:00+01:00")

            if(getPM10Response.isNotEmpty()){
                val average = getPM10Response.map { (it.value).toInt() }
                    .takeIf { it.isNotEmpty() }
                    ?.average() ?: 0.0

                event =  Event(
                    activityName = "Tree Planting Event " + Random.nextInt(0, 99999),
                    points = 10,
                    type = "event",
                    description = "Organized event in a location in Skopje where the air quality is poor (high PM10).",
                    date =  ZonedDateTime.now().plusWeeks(1).toString(),
                    averageValue = average,
                    qrCodeString = "Event_${getPM10Response.first().position}" )

            sensor to event
            }
            else { null }
        }

        var highestAveragePM10Event = Event()
        averageValuesOfPM10PerSensor.forEach { (_, event) ->
            if (highestAveragePM10Event.averageValue == 0.0 || event.averageValue > highestAveragePM10Event.averageValue) {
                highestAveragePM10Event = event
            }
        }

        return highestAveragePM10Event
    }


    private fun formatDateTimeForApi(): Pair<String, String> {
        val now = ZonedDateTime.now()

        val firstDayOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .with(LocalTime.MIDNIGHT)
        val lastDayOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .with(LocalTime.MIDNIGHT)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'")
        val firstDayFormatted = firstDayOfWeek.format(formatter)
        val lastDayFormatted = lastDayOfWeek.format(formatter)

        return Pair(firstDayFormatted, lastDayFormatted)
    }


}