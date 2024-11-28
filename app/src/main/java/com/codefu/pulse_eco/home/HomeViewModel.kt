package com.codefu.pulse_eco.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codefu.pulse_eco.presentation.sign_in.UserData

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

}