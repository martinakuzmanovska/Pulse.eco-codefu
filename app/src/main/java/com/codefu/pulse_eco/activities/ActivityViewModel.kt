package com.codefu.pulse_eco.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.repositories.ActivityRepository
import com.codefu.pulse_eco.domain.repositories.impl.ActivityRepositoryImpl
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val repository: ActivityRepository
): ViewModel() {
    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>>
                 get() = _activities

    fun getActivities(){
        viewModelScope.launch {
            try {
                _activities.value = repository.fetchActivities()
            } catch (e: Exception) {
                _activities.value = emptyList()
                Log.e("ActivityViewModel", "Error fetching activities", e)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        (repository as? ActivityRepositoryImpl)?.detachActivityListener()
    }

}