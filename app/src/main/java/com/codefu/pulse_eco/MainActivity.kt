package com.codefu.pulse_eco

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codefu.pulse_eco.databinding.ActivityMainBinding
import com.codefu.pulse_eco.domain.repositories.ActivityRepository
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.codefu.pulse_eco.service.StepCounterService
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navView.menu.findItem(R.id.navigation_logs).isVisible = false
        navView.menu.findItem(R.id.navigation_event).isVisible = false

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_profile,R.id.navigation_logs, R.id.navigation_dashboard, R.id.navigation_market, R.id.navigation_event,R.id.navigation_home
            )
        )

        // Show items if the user is logged in
        if (!googleAuthUiClient.getSignedInUser()?.userId.isNullOrEmpty()) {
            navView.menu.findItem(R.id.navigation_logs).isVisible = true
            navView.menu.findItem(R.id.navigation_event).isVisible = true
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    val user = googleAuthUiClient.getSignedInUser()
                    if (user == null) {
                        // Navigate to LoginActivity if no user
                        val intent = Intent(this, LogInActivity::class.java)
                        startActivity(intent)
                        true
                    } else {
                        // Navigate to ProfileFragment if user is signed in
                        navController.navigate(R.id.navigation_profile)
                        true
                    }
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }

    }


    private fun addActivity(activityRepository: ActivityRepository) {
        // Launch a coroutine in the IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            activityRepository.createActivities("JSP", 1, "zdravo") { success ->
                if (success) {
                    println("Activity created successfully!")
                } else {
                    println("Failed to create activity.")
                }
            }
        }

    }

    fun updateNavMenuVisibility() {
        val navView: BottomNavigationView = binding.navView
        val isUserSignedIn = !googleAuthUiClient.getSignedInUser()?.userId.isNullOrEmpty()
        navView.menu.findItem(R.id.navigation_logs).isVisible = isUserSignedIn
        navView.menu.findItem(R.id.navigation_event).isVisible = isUserSignedIn
    }


}