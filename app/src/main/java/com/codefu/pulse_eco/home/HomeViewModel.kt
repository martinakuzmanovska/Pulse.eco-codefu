package com.codefu.pulse_eco.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codefu.pulse_eco.presentation.sign_in.UserData
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {


    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    private val _user =  MutableLiveData<UserData>()

    val text: LiveData<String> = _text

    val user : MutableLiveData<UserData> = _user

    fun setUserValue(user:UserData) {
        this._user.value = user
    }

    fun getUserValue(): UserData? {
        return _user.value
    }

    fun fetchUserData(userId: String) {
        viewModelScope.launch {
            try {
                val database = Firebase.database.reference
                val snapshot = database.child("users").child(userId).get().await()
                val userData = snapshot.getValue(UserData::class.java)
                if (userData != null) {
                    _user.postValue(userData!!)
                } else {
                    Log.w("HomeViewModel", "User data is null or malformed for userId=$userId")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to fetch user data", e)
            }
        }
    }



}