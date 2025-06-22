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

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    @SuppressLint("SuspiciousIndentation")
    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
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
            user?.let { checkAndSaveUser(it) }

            SignInResult(
                data = user?.run {
                    // Do NOT set points here — get points from database separately
                    UserData(
                        userId = uid,
                        name = displayName,
                        email = email,
                        points = 0 )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(data = null, errorMessage = e.message)
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
            Log.d("GoogleAuthUiClient", "Successfully signed out")
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    private suspend fun checkAndSaveUser(user: FirebaseUser) {
        val database = Firebase.database.reference
        try {
            val snapshot = database.child("users").child(user.uid).get().await()
            if (snapshot.exists()) {
                Log.d("FirebaseDB", "User already exists in the database")
            } else {
                val userMap = mapOf(
                    "uid" to user.uid,
                    "name" to user.displayName,
                    "email" to user.email,
                    "photoUrl" to user.photoUrl.toString(),
                    "points" to 0,  // Initialize points to 0 on user creation
                    "itemIds" to emptyList<String>() // initialize purchase history empty list

                )
                database.child("users").child(user.uid).setValue(userMap).await()
                Log.d("FirebaseDB", "User saved successfully")
            }
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error checking or saving user", e)
        }
    }

    // Return signed in user basic info from Firebase Auth without points
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            name = displayName,
            email = email,
            points = 0 // points come from database, so set null here
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

    suspend fun updateUserPointsAndItems(userId: String, newPoints: Int, newItemId: String) {
        val database = Firebase.database.reference

        try {
            val userRef = database.child("users").child(userId)

            // Get current itemIds list (if any)
            val snapshot = userRef.get().await()
            val currentItemIds = snapshot.child("itemIds").children.mapNotNull { it.getValue(String::class.java) }

            // Add new item to the list
            val updatedItemIds = currentItemIds.toMutableList().apply { add(newItemId) }

            // Build update map
            val updates = mapOf(
                "points" to newPoints,
                "itemIds" to updatedItemIds
            )

            Log.d("FirebaseDB", newPoints.toString())
            // Apply update
            //userRef.updateChildren(updates).await()
            userRef.child("points").setValue(newPoints).await()
            userRef.child("itemIds").setValue(updatedItemIds).await()

            Log.d("FirebaseDB", "User points and items updated successfully")

        } catch (e: Exception) {
            Log.e("FirebaseDB", "Failed to update user data", e)
        }
    }
}
