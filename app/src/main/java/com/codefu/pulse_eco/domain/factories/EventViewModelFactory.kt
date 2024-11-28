package com.codefu.pulse_eco.domain.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codefu.pulse_eco.domain.repositories.impl.EventRepositoryImpl
import com.codefu.pulse_eco.events.EventViewModel

class EventViewModelFactory:ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            val repository = EventRepositoryImpl()
            return EventViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}