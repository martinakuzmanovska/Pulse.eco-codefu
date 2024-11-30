package com.codefu.pulse_eco.domain.factories

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
import com.codefu.pulse_eco.logs.UserActivityLogViewModel

class UserActivityLogViewModelFactory(private val applicationContext: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(UserActivityLogViewModel::class.java)){
            val repository = UserActivityLogRepositoryImpl(applicationContext)
            return UserActivityLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
