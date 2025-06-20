package com.codefu.pulse_eco

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codefu.pulse_eco.databinding.ActivityMainBinding
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity
import com.codefu.pulse_eco.domain.repositories.ActivityRepository
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.domain.repositories.impl.ActivityRepositoryImpl
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
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
        val activityRepository: ActivityRepository = ActivityRepositoryImpl()
        val activityLogRepository: UserActivityLogRepository = UserActivityLogRepositoryImpl(this)

        val navView: BottomNavigationView = binding.navView

        navView.menu.findItem(R.id.navigation_logs).isVisible = false
        navView.menu.findItem(R.id.navigation_event).isVisible = false

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_profile,
                R.id.navigation_logs,
                R.id.navigation_dashboard,
                R.id.navigation_market,
                R.id.navigation_event
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
                        startActivity(Intent(this, LogInActivity::class.java))
                        false  // Don't select Profile tab if user is not signed in
                    } else {
                        // Only navigate if not already on Profile to avoid duplicate navigation
                        if (navController.currentDestination?.id != R.id.navigation_profile) {
                            navController.navigate(R.id.navigation_profile)
                        }
                        true  // Indicate event handled and selection updated
                    }
                }
                R.id.navigation_pulse_eco -> {
                    openAnotherApp("com.netcetera.skopjepulse")
                    false  // Don't update bottom nav selection because this is external
                }
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }




    }

    private fun openAnotherApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            val playStoreIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            startActivity(playStoreIntent)
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

//    private fun addActivityLog(activityLogRepository: UserActivityLogRepository) {
//        // Launch a coroutine in the IO dispatcher
//        CoroutineScope(Dispatchers.IO).launch {
//            activityLogRepository.createLog( "JSP","Testing",10) { success ->
//                if (success) {
//                    println("Activity Log  created successfully!")
//                } else {
//                    println("Failed to create activity log.")
//                }
//            }
//        }
//
//    }

}