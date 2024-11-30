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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.codefu.pulse_eco.LogInActivity
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.activities.ActivityViewModel
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
        if (checkLocationPermission()) {
            getCurrentLocation()
        } else {
            // Optionally show message if permissions are not granted
            Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show()
        }

        return root
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

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Get address from latitude and longitude
                    val address = getAddressFromLatLong(latitude, longitude)

                    // Set address to TextView
                    binding.locationAddress.text = address
                    Toast.makeText(requireContext(), "Address: $address", Toast.LENGTH_LONG).show()

                } else {
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SecurityException) {
            // Catch the exception if permission is not granted
            Toast.makeText(requireContext(), "Permission denied or revoked: ${e.message}", Toast.LENGTH_LONG).show()
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

    // Handle permissions request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
