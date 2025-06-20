package com.codefu.pulse_eco.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codefu.pulse_eco.domain.models.Event
import com.codefu.pulse_eco.domain.repositories.EventRepository
import com.codefu.pulse_eco.domain.repositories.impl.EventRepositoryImpl
import com.codefu.pulse_eco.presentation.sign_in.UserData
import kotlinx.coroutines.launch

class EventViewModel( private val repository: EventRepository) :ViewModel(){

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    private val _user =  MutableLiveData<UserData>()

    val user : MutableLiveData<UserData> = _user

    fun setUserValue(user: UserData) {
        this._user.value = user
    }

    fun getEvents(){
        viewModelScope.launch {
            try {
                _events.value = repository.fetchEvents()
            } catch (e: Exception) {
                _events.value = emptyList()
                Log.e("EventViewModel", "Error fetching events", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        (repository as? EventRepositoryImpl)?.detachActivityListener()
    }
}