package com.codefu.pulse_eco

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.codefu.pulse_eco.databinding.ActivityLogInBinding
import com.codefu.pulse_eco.databinding.ActivityMainBinding
import com.codefu.pulse_eco.presentation.profile.ProfileScreen
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.codefu.pulse_eco.presentation.sign_in.SignInResult
import com.codefu.pulse_eco.presentation.sign_in.SignInScreen
import com.codefu.pulse_eco.presentation.sign_in.SignInState
import com.codefu.pulse_eco.presentation.sign_in.SignInViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.codefu.pulse_eco.theme.PulseEcoCodeFuTheme
import kotlinx.coroutines.launch
class LogInActivity : AppCompatActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        actionBar?.hide()

        // Adjust window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Register the ActivityResultLauncher for sign-in intents
        launcher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                   handleSignInResult(signInResult)
                }
            }
        }

        // Set click listener on the Google ImageView
        val googleImageView: ImageView = findViewById(R.id.google_sign_in)
        googleImageView.setOnClickListener {
            initiateSignIn()
        }
    }

    private fun initiateSignIn() {
        lifecycleScope.launch {
            val signInIntentSender = googleAuthUiClient.signIn()
            if (signInIntentSender != null) {
                launcher.launch(IntentSenderRequest.Builder(signInIntentSender).build())
            } else {
                Toast.makeText(this@LogInActivity, "Error initiating sign-in.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignInResult(signInResult: SignInResult) {
        if (signInResult.errorMessage.isNullOrEmpty()) {
            Toast.makeText(this, "Sign in successful", Toast.LENGTH_LONG).show()
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "Sign in failed: ${signInResult.errorMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
