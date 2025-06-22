
package com.codefu.pulse_eco.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.apiClients.PulseEcoApiProvider
import com.codefu.pulse_eco.apiClients.dataModels.findClosestSensor
import com.codefu.pulse_eco.databinding.FragmentHomeBinding
import com.codefu.pulse_eco.domain.factories.EventViewModelFactory
import com.codefu.pulse_eco.events.EventViewModel
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

class HomeFragment : Fragment() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireActivity().applicationContext,
            oneTapClient = Identity.getSignInClient(requireActivity().applicationContext)
        )
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        val eventViewModel = ViewModelProvider(this, EventViewModelFactory())[EventViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        googleAuthUiClient.getSignedInUser()?.let { homeViewModel.setUserValue(it) }

        val headerProfileTitle = binding.includeHeader.profileTitle
        homeViewModel.user.observe(viewLifecycleOwner) {
            headerProfileTitle.text = homeViewModel.user.value?.name.toString()
        }

        eventViewModel.getEvents()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Check for location permissions and get current location

//        if (checkLocationPermission()) {
//          getCurrentLocation()
//
//        } else {
//            // Optionally show message if permissions are not granted
//            Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show()
//        }

        return binding.root
    }

    private fun calculateCAQI(pm10: Double, pm25: Double): Int {
        val pm10Index = (pm10 / 50) * 100  // 50µg/m³ = CAQI 100
        val pm25Index = (pm25 / 25) * 100  // 25µg/m³ = CAQI 100

        return max(pm10Index, pm25Index).toInt().coerceAtMost(500) // Cap at 500
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (checkLocationPermission()) {
                try {
                    val latLong = getCurrentLocation()
                    val result = getValuesForParticles(latLong)

                    // Enhanced time display
                    val currentTime = LocalTime.now()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val formattedTime = currentTime.format(formatter)

                    val pm10Value = result.pm10.toDoubleOrNull() ?: 0.0
                    val pm25Value = result.pm25.toDoubleOrNull() ?: 0.0
                    val caqi = calculateCAQI(pm10Value, pm25Value)

                    binding.caqiValue.text = caqi.toString()
                    binding.time.text = " $formattedTime"
                    binding.pm10Value.text = result.pm10 + "µg/m³"
                    binding.pm25Value.text = result.pm25 + "µg/m³"
                    binding.noiseValue.text = result.noise + "dbA"

                    updateAirQualityMessage(caqi)

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateAirQualityMessage(caqi: Int) {
        val (message, colorRes) = when {
            caqi < 25 -> Pair("Excellent air!", R.color.green)
            caqi < 50 -> Pair("Good air", R.color.light_green)
            caqi < 75 -> Pair("Moderate", R.color.yellow)
            caqi < 100 -> Pair("Poor", R.color.orange)
            else -> Pair("Very poor", R.color.red)
        }

        binding.airQualityMessage.text = message
        binding.airQualityMessage.setTextColor(ContextCompat.getColor(requireContext(), colorRes))

        // Update smiley based on CAQI
        val smileyRes = when {
            caqi < 25 -> R.drawable.ic_smile_happy
            caqi < 50 -> R.drawable.ic_smile
            caqi < 75 -> R.drawable.ic_smile_neutral
            caqi < 100 -> R.drawable.ic_smile_sad
            else -> R.drawable.ic_smile_very_sad
        }
        binding.smileIcon.setImageResource(smileyRes)
    
    }


    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            // Request permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            false
        }
    }

    private suspend fun getCurrentLocation(): LatLong {
        return suspendCoroutine { continuation ->
            try {
                val locationRequest = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY

                fusedLocationClient.getCurrentLocation(locationRequest, null)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude

                            val address = getAddressFromLatLong(latitude, longitude)
                            _binding?.let { safeBinding ->
                                safeBinding.locationAddress.text = address
                            }

                            Toast.makeText(requireContext(), "Address: $address", Toast.LENGTH_LONG).show()

                            continuation.resume(LatLong(latitude, longitude))
                        } else {
                            Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_LONG).show()
                            continuation.resumeWith(Result.failure(Exception("Location is null")))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                        continuation.resumeWith(Result.failure(exception))
                    }

            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Permission denied: ${e.message}", Toast.LENGTH_LONG).show()
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

    private fun getAddressFromLatLong(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0) // Full address as a single string
            } else {
                "Address not found"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private suspend  fun  getValuesForParticles(latLong: LatLong) : ValuesPulseEco{
        val api = PulseEcoApiProvider.getPulseEcoApi()
        val sensors = api.getAllPulseEcoSensors()
        val sensorId = findClosestSensor(latLong.lat, latLong.long,sensors)?.sensorId
        var noise = ""
        var pm10 = ""
        var pm25 = ""
        var temperature = ""
        for (s in sensors){
            if (sensorId == s.sensorId && s.type == "noise_dba" )
            {
                noise = s.value
            }
            else if (sensorId == s.sensorId && s.type == "pm10" )
            {
                pm10 = s.value
            }
            else if (sensorId == s.sensorId && s.type == "pm25" ) {
                pm25 = s.value
            }
            else if (sensorId == s.sensorId && s.type == "temperature" ) {
                temperature = s.value
            }


        }
        // pm 10, pm25, noise, temperature
        return ValuesPulseEco(noise,pm10,pm25,temperature)
    }


    // Handle permissions request result
    @Suppress("deprecation")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                lifecycleScope.launch {
                    getCurrentLocation()
                    // Use the retrieved location
                    Toast.makeText(requireContext(), "Permission granted!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class LatLong(val lat: Double, val long:Double)

data class ValuesPulseEco(val noise: String, val pm10: String, val pm25: String, val temperature: String)
