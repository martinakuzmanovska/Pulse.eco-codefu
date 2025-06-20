package com.codefu.pulse_eco.logs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codefu.pulse_eco.domain.models.UserActivityLog
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
import com.codefu.pulse_eco.presentation.sign_in.UserData
import kotlinx.coroutines.launch

class UserActivityLogViewModel (
    private val repository: UserActivityLogRepository): ViewModel(){
        private val _logs = MutableLiveData<List<UserActivityLog>>()
    val logs : LiveData<List<UserActivityLog>>
        get() = _logs

    private val _user =  MutableLiveData<UserData>()

    val user : MutableLiveData<UserData> = _user

    fun setUserValue(user: UserData) {
        this._user.value = user
    }


    fun getLogs(userId: String) {
        viewModelScope.launch {
            try {
                _logs.value = repository.fetchUserLogs(userId)
            } catch (e: Exception) {
                _logs.value = emptyList()
                Log.e("UserActivityLogViewModel", "Error fetching User's logs", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        (repository as? UserActivityLogRepositoryImpl)?.detachUserActivityLogListener()
    }
}