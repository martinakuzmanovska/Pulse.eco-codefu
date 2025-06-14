package com.codefu.pulse_eco.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.codefu.pulse_eco.LogInActivity
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.activities.ActivityViewModel
import com.codefu.pulse_eco.apiClients.PulseEcoApiProvider
import com.codefu.pulse_eco.apiClients.dataModels.findClosestSensor
import com.codefu.pulse_eco.databinding.FragmentHomeBinding
import com.codefu.pulse_eco.domain.factories.ActivityViewModelFactory
import com.codefu.pulse_eco.domain.factories.EventViewModelFactory
import com.codefu.pulse_eco.domain.repositories.ActivityRepository
import com.codefu.pulse_eco.events.EventViewModel
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.LocationServices
import java.util.Locale

import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

        val textView: TextView = binding.userFullName
        homeViewModel.user.observe(viewLifecycleOwner) {
            textView.text = homeViewModel.user.value?.name.toString()
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

        return root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (checkLocationPermission()) {
                try {
                    val latLong = getCurrentLocation()
                    val result = getValuesForParticles(latLong)
                    val currentTime = LocalTime.now()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm") // 24-hour format. Use "hh:mm a" for 12-hour with AM/PM
                    val formattedTime = currentTime.format(formatter)
                    binding.time.text = formattedTime.toString()
                    binding.pm10Value.text = result.pm10 + "µg/m³"
                    binding.pm25Value.text = result.pm25 + "µg/m³"
                    binding.noiseValue.text = result.noise + "dbA"
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted
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
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        // Get address from latitude and longitude
                        val address = getAddressFromLatLong(latitude, longitude)

                        // Update UI
                        binding.locationAddress.text = address
                        Toast.makeText(requireContext(), "Address: $address", Toast.LENGTH_LONG).show()

                        continuation.resume(LatLong(latitude, longitude))
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_LONG).show()
                        continuation.resumeWith(Result.failure(Exception("Location is null")))
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                    continuation.resumeWith(Result.failure(exception))
                }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Permission denied or revoked: ${e.message}", Toast.LENGTH_LONG).show()
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
                    val location = getCurrentLocation()
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
