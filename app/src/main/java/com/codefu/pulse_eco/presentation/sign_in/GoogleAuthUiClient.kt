package com.codefu.pulse_eco.presentation.sign_in

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.codefu.pulse_eco.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await


class GoogleAuthUiClient (private val context: Context,
                          private val oneTapClient: SignInClient){
    private val auth = Firebase.auth

    @SuppressLint("SuspiciousIndentation")
    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e
              null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            user?.let { saveUserToDatabase(it) }
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        name = displayName,
                        email = email
                    )
                }, errorMessage = null
            )

        }
        catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(data = null, errorMessage = e.message)
        }

    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    private fun saveUserToDatabase(user: FirebaseUser) {
        val database = Firebase.database.reference

        // Check if the user already exists
        database.child("users").child(user.uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // User already exists
                    Log.d("FirebaseDB", "User already exists in the database")
                } else {
                    // User does not exist, save to database
                    val userMap = mapOf(
                        "uid" to user.uid,
                        "name" to user.displayName,
                        "email" to user.email,
                        "photoUrl" to user.photoUrl.toString()
                    )

                    database.child("users").child(user.uid).setValue(userMap)
                        .addOnSuccessListener {
                            Log.d("FirebaseDB", "User saved successfully")
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseDB", "Failed to save user", it)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("FirebaseDB", "Error checking user existence", it)
            }
    }


    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            name = displayName,
            email = email
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}