package com.codefu.pulse_eco.domain.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codefu.pulse_eco.domain.repositories.ShopItemRepository
import com.codefu.pulse_eco.domain.repositories.impl.EventRepositoryImpl
import com.codefu.pulse_eco.domain.repositories.impl.ShopItemRepositoryImpl
import com.codefu.pulse_eco.events.EventViewModel
import com.codefu.pulse_eco.market.ShopItemViewModel
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
class ShopItemModelFactory(
    private val repository: ShopItemRepository,
    private val authClient: GoogleAuthUiClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopItemViewModel::class.java)) {
            return ShopItemViewModel(repository, authClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
