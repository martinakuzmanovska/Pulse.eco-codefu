package com.codefu.pulse_eco.domain.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codefu.pulse_eco.domain.repositories.impl.ActivityRepositoryImpl
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
import com.codefu.pulse_eco.logs.UserActivityLogViewModel

class UserActivityLogViewModelFactory: ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(UserActivityLogViewModel::class.java)){
            val repository = UserActivityLogRepositoryImpl()
            return UserActivityLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
